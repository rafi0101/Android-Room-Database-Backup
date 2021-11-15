# Android-Room-Database-Backup Changelog

## [v1.0.0-beta07] - 2021-11-14

### Features
- **Custom Backup Location:** ([#9](https://github.com/rafi0101/Android-Room-Database-Backup/issues/9))

  You can choose between internal, external application storage and (new) custom storage. If you choose custom storage a CreateDocument() (for creating a backup file) or OpenDocument() (for choosing a backup file to restore) Activity will be opened

### Upgrading Notes
- ```.context(this)``` changed to ```val backup = RoomBackup(this)``` and must be declared outside of an onClickListener

- ```.useExternalStorage(false)``` is replaced with ```.backupLocation(backupLocation: Int)```. See [README.md](readme.md)
