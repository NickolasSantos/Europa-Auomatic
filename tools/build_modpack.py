#!/usr/bin/env python3
"""Gera o modpack da CurseForge (build/Europa-RPG-<versão>.zip) a partir de modpack/modpack.json.

- Mods "curseforge": referenciados por projectID/fileID (a CurseForge baixa na importação).
- Mods "bundled": licença livre; baixados do Modrinth na versão testada (hash SHA-1 conferido) e
  colocados em overrides/mods.
- O jar do Europa RPG vem de build/libs (rode ./gradlew build antes).
"""
import hashlib, json, os, shutil, sys, urllib.parse, urllib.request, zipfile

ROOT = os.path.join(os.path.dirname(os.path.abspath(__file__)), "..")
UA = {"User-Agent": "europa-rpg-modpack-builder"}


def get(url):
    return urllib.request.urlopen(urllib.request.Request(url, headers=UA), timeout=120)


def main():
    spec = json.load(open(os.path.join(ROOT, "modpack", "modpack.json"), encoding="utf-8"))
    out_dir = os.path.join(ROOT, "build", "modpack")
    shutil.rmtree(out_dir, ignore_errors=True)
    shutil.copytree(os.path.join(ROOT, "modpack", "overrides"), os.path.join(out_dir, "overrides"))
    mods = os.path.join(out_dir, "overrides", "mods")
    os.makedirs(mods, exist_ok=True)

    for b in spec["bundled"]:
        q = urllib.parse.urlencode({"game_versions": f'["{spec["minecraft"]}"]', "loaders": '["fabric"]'})
        versions = json.load(get(f"https://api.modrinth.com/v2/project/{b['modrinth']}/version?{q}"))
        v = next(x for x in versions if x["version_number"] == b["version"])
        f = next(x for x in v["files"] if x["filename"] == b["file"])
        data = get(f["url"]).read()
        if hashlib.sha1(data).hexdigest() != f["hashes"]["sha1"]:
            sys.exit(f"hash inválido: {b['file']}")
        open(os.path.join(mods, b["file"]), "wb").write(data)
        print("embutido:", b["file"])

    jar = os.path.join(ROOT, "build", "libs", f"europa-rpg-{spec['version']}.jar")
    shutil.copy(jar, mods)

    manifest = {
        "minecraft": {"version": spec["minecraft"], "modLoaders": [{"id": spec["loader"], "primary": True}]},
        "manifestType": "minecraftModpack", "manifestVersion": 1,
        "name": spec["name"], "version": spec["version"], "author": spec["author"],
        "files": [{"projectID": m["projectID"], "fileID": m["fileID"], "required": True} for m in spec["curseforge"]],
        "overrides": "overrides",
    }
    json.dump(manifest, open(os.path.join(out_dir, "manifest.json"), "w"), indent=2)
    rows = "".join(f"<li>{m['name']} ({m['file']})</li>" for m in spec["curseforge"])
    rows += "".join(f"<li>{b['name']} {b['version']} (incluído, {b['license']})</li>" for b in spec["bundled"])
    open(os.path.join(out_dir, "modlist.html"), "w", encoding="utf-8").write(f"<ul>{rows}<li>Europa RPG {spec['version']}</li></ul>")
    notice = ["Mods de terceiros incluídos neste pacote (redistribuídos conforme suas licenças):"]
    notice += [f"- {b['name']} {b['version']} — {b['license']} — código-fonte: https://modrinth.com/mod/{b['modrinth']}" for b in spec["bundled"]]
    open(os.path.join(out_dir, "THIRD_PARTY_NOTICES.txt"), "w", encoding="utf-8").write("\n".join(notice) + "\n")

    zip_path = os.path.join(ROOT, "build", f"Europa-RPG-{spec['version']}.zip")
    with zipfile.ZipFile(zip_path, "w", zipfile.ZIP_DEFLATED) as z:
        for base, _, files in os.walk(out_dir):
            for name in files:
                full = os.path.join(base, name)
                z.write(full, os.path.relpath(full, out_dir))
    print("modpack:", zip_path, os.path.getsize(zip_path) // 1024, "KB")


if __name__ == "__main__":
    main()
