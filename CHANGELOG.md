# Android-Room-Database-Backup Changelog

## [v1.0.0-beta09] - 2021-12-06

### Breaking Changes
- Package name changes from `com.ebner.roomdatabasebackup.core.RoomBackup` to `de.raphaelebner.roomdatabasebackup.core.RoomBackup`

--> Next step is to change from Jitpack to MavenCentral

### Upgrading Notes
- You need to import the library by the new package name and delete the old import. Your IDE may helps you with these steps.

## [v1.0.0-beta08] - 2021-11-14

### Features
- **Custom Backup Location:** ([#9](https://github.com/rafi0101/Android-Room-Database-Backup/issues/9))

  You can choose between internal, external application storage and (new) custom storage. If you choose custom storage a CreateDocument() (for creating a backup file) or OpenDocument() (for choosing a backup file to restore) Activity will be opened

### Upgrading Notes
- ```.context(this)``` changed to ```val backup = RoomBackup(this)``` and must be declared outside of an onClickListener

- ```.useExternalStorage(false)``` is replaced with ```.backupLocation(backupLocation: Int)```. See [README.md](readme.md)
