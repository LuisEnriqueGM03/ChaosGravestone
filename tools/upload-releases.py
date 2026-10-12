"""Validate and publish the three Minecraft 26.2 releases. Token stays in the process environment."""
from pathlib import Path
import hashlib
import json
import os
import sys
import tomllib
import urllib.error
import urllib.request
import uuid
import zipfile

ROOT = Path(__file__).resolve().parent.parent
PROJECT = 1728749
API = 'https://minecraft.curseforge.com/api'
TOKEN = os.environ.get('CURSEFORGE_TOKEN', '')
RECORD = ROOT / 'release-records-26.2.json'


def request(path, data=None, content_type=None):
    headers = {'X-Api-Token': TOKEN, 'User-Agent': 'ChaosGravestone/1.1.0'}
    if content_type:
        headers['Content-Type'] = content_type
    req = urllib.request.Request(API + path, data=data, headers=headers)
    try:
        with urllib.request.urlopen(req, timeout=60) as response:
            return json.load(response)
    except urllib.error.HTTPError as error:
        detail = error.read().decode(errors='replace')
        if TOKEN:
            detail = detail.replace(TOKEN, '[redacted]')
        raise RuntimeError(f'CurseForge HTTP {error.code}: {detail[:1200]}') from None


def main():
    props = dict(line.split('=', 1) for line in (ROOT / 'gradle.properties').read_text(encoding='utf-8').splitlines()
                 if '=' in line and not line.startswith('#'))
    mc, version = props['minecraft_version'], props['version']
    assert mc == '26.2'
    artifacts = []
    for loader in ('fabric', 'neoforge', 'forge'):
        path = ROOT / loader / 'build/libs' / f'chaosgravestone-{loader}-{mc}-{version}.jar'
        contents = path.read_bytes()
        digest = hashlib.sha256(contents).hexdigest()
        with zipfile.ZipFile(path) as jar:
            assert jar.read('com/chaos/gravestone/ChaosGravestone.class')[6:8] == bytes([0, 69]), 'Java 25 target required'
            assert json.loads(jar.read('pack.mcmeta'))['pack']['min_format'] == [88, 0]
            assert 'data/chaosgravestone/recipe/ender_grave_compass.json' in jar.namelist()
            assert 'META-INF/services/com.chaos.gravestone.platform.Platform' in jar.namelist()
            assert json.loads(jar.read('chaosgravestone.mixins.json'))['compatibilityLevel'] == ('JAVA_21' if loader == 'forge' else 'JAVA_25')
            for name in ('grave_compass', 'ender_grave_compass'):
                model = json.loads(jar.read(f'assets/chaosgravestone/items/{name}.json'))['model']
                assert model['property'] == 'minecraft:compass' and model['target'] == 'lodestone'
                assert len(model['entries']) == 33
            if loader == 'fabric':
                metadata = json.loads(jar.read('fabric.mod.json'))
                assert metadata['version'] == version
                assert metadata['depends']['minecraft'] == '26.2'
                assert metadata['authors'] == ['Goddark83']
            else:
                metadata_path = 'META-INF/neoforge.mods.toml' if loader == 'neoforge' else 'META-INF/mods.toml'
                descriptor = tomllib.loads(jar.read(metadata_path).decode())
                dependencies = {d['modId']: d for d in descriptor['dependencies']['chaosgravestone']}
                assert dependencies['minecraft']['versionRange'] == props['minecraft_version_range']
                expected_range = '[26.2.0.90,26.3)' if loader == 'neoforge' else '[65.1.3,66)'
                assert dependencies[loader]['versionRange'] == expected_range
                metadata = descriptor['mods'][0]
                assert metadata['version'] == version and metadata['modId'] == 'chaosgravestone'
                assert metadata['authors'] == 'Goddark83'
        artifacts.append((loader, path, contents, digest))
        print(json.dumps({'loader': loader, 'jar': path.name, 'sha256': digest, 'validation': 'OK'}), flush=True)
    if '--validate' in sys.argv:
        return
    if not TOKEN:
        raise RuntimeError('CURSEFORGE_TOKEN is required')
    versions = request('/game/versions')
    def tag(name):
        return next(v['id'] for v in versions if v['name'] == name)
    records = json.loads(RECORD.read_text(encoding='utf-8')) if RECORD.exists() else []
    for loader, path, contents, digest in artifacts:
        previous = next((r for r in records if r['loader'] == loader and r['sha256'] == digest), None)
        if previous:
            print(json.dumps({'loader': loader, 'status': 'already_uploaded', 'url': previous['url']}), flush=True)
            continue
        label = {'fabric': 'Fabric', 'neoforge': 'NeoForge', 'forge': 'Forge'}[loader]
        metadata = {
            'displayName': f'Chaos Gravestone {version} - {label} {mc}',
            'changelogType': 'markdown',
            'changelog': (ROOT / 'tools/changelog.md').read_text(encoding='utf-8'),
            'gameVersions': [tag(mc), tag(label), tag('Java 25'), tag('Client'), tag('Server')],
            'releaseType': 'release',
        }
        if loader == 'fabric':
            metadata['relations'] = {'projects': [{'slug': 'fabric-api', 'type': 'requiredDependency'}]}
        print(json.dumps({'loader': loader, 'metadata': metadata}), flush=True)
        if '--upload' not in sys.argv:
            continue
        boundary = 'ChaosGravestone' + uuid.uuid4().hex
        body = (
            (f'--{boundary}\r\nContent-Disposition: form-data; name="metadata"\r\nContent-Type: application/json\r\n\r\n').encode()
            + json.dumps(metadata).encode()
            + (f'\r\n--{boundary}\r\nContent-Disposition: form-data; name="file"; filename="{path.name}"\r\nContent-Type: application/java-archive\r\n\r\n').encode()
            + contents + (f'\r\n--{boundary}--\r\n').encode()
        )
        result = request(f'/projects/{PROJECT}/upload-file', body, 'multipart/form-data; boundary=' + boundary)
        file_id = result['id']
        record = {'project_id': PROJECT, 'loader': loader, 'minecraft': mc, 'version': version,
                  'file_id': file_id, 'sha256': digest,
                  'url': f'https://www.curseforge.com/minecraft/mc-mods/chaos-gravestone/files/{file_id}'}
        records.append(record)
        RECORD.write_text(json.dumps(records, indent=2) + '\n', encoding='utf-8')
        print(json.dumps(record), flush=True)


if __name__ == '__main__':
    main()
