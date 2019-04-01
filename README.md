# Pack Creator

Creates modpack in HMCL format.

## Usage

To start, you need `hmcl.json`, `modpack.json` and `pack.json`. They are all the same format
with HMCL exported version.

We recommended exporting your manual setup from HMCL and copy them.

However, while you are not getting along with HMCL, here are some examples for those files.

`hmcl.json`:

```json
{
  "last": "",
  "backgroundType": "DEFAULT",
  "commonDirType": "DEFAULT",
  "hasProxy": false,
  "hasProxyAuth": false,
  "proxyType": "HTTP",
  "proxyPort": 0,
  "theme": "#5c6bc0",
  "localization": "def",
  "downloadType": "bmclapi",
  "configurations": {},
  "accounts": [],
  "fontFamily": "Consolas",
  "fontSize": 12.0,
  "logLines": 100,
  "authlibInjectorServers": [],
  "updateChannel": "STABLE",
  "_version": 0,
  "uiVersion": 0,
  "preferredLoginType": "yggdrasil"
}
```

`modpack.json`:

```
{
  "name": "",
  "author": "",
  "version": "1.0",
  "gameVersion": "1.12.2",
  "description": ""
}
```

`pack.json` is which we called `<version>.json` in versions folder.