"""Validate and publish the three Minecraft 1.20.1 releases. Token stays in the process environment."""
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
RECORD = ROOT / 'release-records-1.20.1.json'


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
    props = dict(line.split('=', 1) for line in (ROOT / 'gradle.properties').read_text().splitlines()
                 if '=' in line and not line.startswith('#'))
    mc, version = props['minecraft_version'], props['version']
    assert mc == '1.20.1'
    artifacts = []
    for loader in ('fabric', 'neoforge', 'forge'):
        path = ROOT / loader / 'build/libs' / f'chaosgravestone-{loader}-{mc}-{version}.jar'
        contents = path.read_bytes()
        digest = hashlib.sha256(contents).hexdigest()
        with zipfile.ZipFile(path) as jar:
            assert jar.read('com/chaos/gravestone/ChaosGravestone.class')[6:8] == bytes([0, 61]), 'Java 17 target required'
            assert json.loads(jar.read('pack.mcmeta'))['pack']['pack_format'] == 15
            assert 'data/chaosgravestone/recipes/ender_grave_compass.json' in jar.namelist()
            assert 'META-INF/services/com.chaos.gravestone.platform.Platform' in jar.namelist()
            refmap = json.loads(jar.read('chaosgravestone.refmap.json'))
            assert refmap['mappings'], 'Missing production mixin mappings'
            for name in ('grave_compass', 'ender_grave_compass'):
                assert len(json.loads(jar.read(f'assets/chaosgravestone/models/item/{name}.json'))['overrides']) == 32
            if loader == 'fabric':
                metadata = json.loads(jar.read('fabric.mod.json'))
                assert metadata['version'] == version
                assert metadata['depends']['minecraft'] == '~1.20.1'
                assert metadata['authors'] == ['Goddark83']
            else:
                metadata = tomllib.loads(jar.read('META-INF/mods.toml').decode())['mods'][0]
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
    records = json.loads(RECORD.read_text()) if RECORD.exists() else []
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
            'gameVersions': [tag(mc), tag(label), tag('Java 17'), tag('Client'), tag('Server')],
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
        RECORD.write_text(json.dumps(records, indent=2) + '\n')
        print(json.dumps(record), flush=True)


if __name__ == '__main__':
    main()
