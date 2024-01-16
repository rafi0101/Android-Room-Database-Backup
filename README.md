# Android-Room-Database-Backup


![Build](https://github.com/rafi0101/Android-Room-Database-Backup/workflows/Android%20CI/badge.svg)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/de.raphaelebner/roomdatabasebackup/badge.svg)](https://maven-badges.herokuapp.com/maven-central/de.raphaelebner/roomdatabasebackup)
[![Room Version](https://img.shields.io/badge/room_version-2.6.1-orange)](https://developer.android.com/jetpack/androidx/releases/room#2.6.1)
[![API](https://img.shields.io/badge/API-21%2B-brightgreen.svg?style=flat)](https://android-arsenal.com/api?level=21)
![Language](https://img.shields.io/badge/language-Kotlin-orange.svg)
[![PRWelcome](https://img.shields.io/badge/PRs-welcome-brightgreen.svg)](https://github.com/rafi0101/Android-Room-Database-Backup/pulls)
[![License](https://img.shields.io/badge/license-MIT-blue.svg)](https://github.com/rafi0101/Android-Room-Database-Backu/blob/master/LICENSE)

<div align="center">
  <sub>Built with ❤︎ by
  <a href="https://github.com/rafi0101">Raphael Ebner</a>
</div>
<br/>

Simple tool to backup and restore your room database in Android

Features
---------
* Create simple backups of your room database
* Encrypt the backup file with AES encryption
* Save the backup to any type of storage
* Material design
* Written in Kotlin

Content
-----------
* [Features](#Features)
* [Changelog](#Changelog)
* [Getting started](#Getting-started)
* [Usage](#Usage)
* [Sample app](#Sample-app)
* [Developed by](#Developed-by)
* [License](#License)

Changelog
-----------
[Changelog and Upgrading notes](CHANGELOG.md)

Getting started
-----------

Android-Room-Database-Backup library is pushed
to [Maven Central](https://central.sonatype.com/artifact/de.raphaelebner/roomdatabasebackup/1.0.0-beta14/versions)
.  
Add the dependency for `Android-Room-Database-Backup ` to your app-level `build.gradle` file.

```groovy
implementation 'de.raphaelebner:roomdatabasebackup:1.0.0-beta14'
```

**If this version makes any technical problems please feel free to contact me. I made some changes in Gradle/Kotlin DSL and not sure if everything is working as excepted**  

Usage
-----------

* [Properties](#Properties)
* [Exit Codes](#Exit-Codes)
* [Example Activity (Kotlin and Java)](#example-activity-kotlin-and-java)
* [Example Fragment (Java and Kotlin)](#example-fragment-kotlin-and-java)

### Properties

**Required**

* Current context  
  **Attention**  
    Must be declared outside of an onClickListener before lifecycle state changes to started

    ```kotlin
    RoomBackup(this)
    ```

  * Instance of your room database


    ```kotlin
    .database(*YourDatabase*.getInstance(this))
    ```

e.g. [`YourDatabase.kt`](app/src/main/java/de/raphaelebner/roomdatabasebackup/sample/database/main/FruitDatabase.kt)

**Optional**

The following options are optional and the default options

  * Enable logging, for debugging and some error messages


    ```kotlin
    .enableLogDebug(false)
    ```

  * Set custom log tag


    ```kotlin
    .customLogTag("debug_RoomBackup")
    ```   

  * Enable and set maxFileCount
      * if file count of Backups > maxFileCount all old / the oldest backup file will be deleted
      * can be used with internal and external storage
      * default: infinity


    ```kotlin
    .maxFileCount(5)
    ```

  * Encrypt your backup
      * Is encrypted with AES encryption
      * uses a random 15 digit long key with alphanumeric characters
      * this key is saved in [EncryptedSharedPreferences](https://developer.android.com/reference/androidx/security/crypto/EncryptedSharedPreferences)
      * backup name is default backup name + ".aes"


    ```kotlin
    .backupIsEncrypted(false)
    ```

  * Encrypt your backup with your own password / key
      * This property is only working, if ```.backupIsEncrypted(true)``` is set
      * If you use the key to encrypt the backup, you will also need it to decrypt
      * Example: If you want to create an encrypted backup, export it and import it to another device. Then you need a custom key, else the backup is encrypted with a random key, and you can not decrypt it on a new device
      
      **Attention**  
      i do not assume any liability for the loss of your key

 
    ```kotlin
    .customEncryptPassword("YOUR_SECRET_PASSWORD")
    ```

  * Save your backup to different storage
      * External
          * storage path: /storage/emulated/0/Android/data/*package*/files/backup/
          * This files will be deleted, if you uninstall your app
          * ```RoomBackup.BACKUP_FILE_LOCATION_EXTERNAL```
      * Internal
          * Private, storage not accessible
          * This files will be deleted, if you uninstall your app
          * ```RoomBackup.BACKUP_FILE_LOCATION_INTERNAL```
      * Custom Dialog
          * You can choose to save or restore where ever you want. A CreateDocument() or OpenDocument() Activity will be launched where you can choose the location
          * If your backup is encrypted I reccomend you using a custom encrption password else you can't restore your backup
          * ```RoomBackup.BACKUP_FILE_LOCATION_CUSTOM_DIALOG```
      * Custom File
          * You can choose to save or restore to/from a custom File. 
          * If your backup is encrypted I reccomend you using a custom encrption password else you can't restore your backup
          * Please use ```backupLocationCustomFile(File)``` to set a custom File
          * ```RoomBackup.BACKUP_FILE_LOCATION_CUSTOM_FILE```

      **Attention**  
      For custom dialog and custom file I only verified the functionality for local storage. For thirt party storage please try and contact me if it is not working. I hope I can find a solution and fix it :)

 
    ```kotlin
    .backupLocation(RoomBackup.BACKUP_FILE_LOCATION_INTERNAL)
    ```

  * Set a custom File to save/restore to/from  
    Only working if ```backupLocation``` is set to ```BACKUP_FILE_LOCATION_CUSTOM_FILE```  
    You have to define a File withe Filename and extension

    ```kotlin
    .backupLocationCustomFile(backupLocationCustomFile: File)
    ```

  * Set a custom dialog title, when showing list of available backups to restore (only for external or internal storage)


    ```kotlin
    .customRestoreDialogTitle("Choose file to restore")
    ```

  * Set your custom name to the Backup files

    **Attention**\
    If a backup file with the same name already exists, it will be replaced


    ```kotlin
    .customBackupFileName(*DatabaseName* + *currentTime* + ".sqlite3")
    ```

  * Run some code, after backup / restore process is finished
      * success: Boolean (If backup / restore was successful = true)
      * message: String (message with simple hints, if backup / restore failed)

    ```kotlin
    .onCompleteListener { success, message, exitCode ->
    }
    ```

  * Restart your Application. Can be implemented in the onCompleteListener, when "success == true"

    **Attention**\
    it does not always work reliably!\
    But you can use other methods.\
    Important is that all activities / fragments that are still open must be closed and reopened\
    Because the Database instance is a new one, and the old activities / fragments are trying to
    work with the old instance

    ```kotlin
    .restartApp(Intent(this@MainActivity, MainActivity::class.java))
    ```

### Exit Codes

Here are all exit codes for the onCompleteListener.  
They can be calles using ```OnCompleteListener.$NAME$```

| Exit Code | Name                                                 | Description  |
| --------- | :--------------------------------------------------- | ------------ |
| 0         | ```EXIT_CODE_SUCCESS```                              | No error, action successful |
| 1         | ```EXIT_CODE_ERROR```                                | Other Error |
| 2         | ```EXIT_CODE_ERROR_BACKUP_FILE_CHOOSER```            | Error while choosing backup to restore. Maybe no file selected |
| 3         | ```EXIT_CODE_ERROR_BACKUP_FILE_CREATOR```            | Error while choosing backup file to create. Maybe no file selected |
| 4         | ```EXIT_CODE_ERROR_BACKUP_LOCATION_FILE_MISSING```   | [BACKUP_FILE_LOCATION_CUSTOM_FILE] is set but [RoomBackup.backupLocationCustomFile] is not set |
| 5         | ```EXIT_CODE_ERROR_BACKUP_LOCATION_MISSING```        | [RoomBackup.backupLocation] is not set |
| 6         | ```EXIT_CODE_ERROR_BY_USER_CANCELED```               | Restore dialog for internal/external storage was canceled by user |
| 7         | ```EXIT_CODE_ERROR_DECRYPTION_ERROR```               | Cannot decrypt provided backup file |
| 8         | ```EXIT_CODE_ERROR_ENCRYPTION_ERROR```               | Cannot encrypt database backup |
| 9         | ```EXIT_CODE_ERROR_RESTORE_BACKUP_IS_ENCRYPTED```    | You tried to restore a encrypted backup but [RoomBackup.backupIsEncrypted] is set to false |
| 10        | ```EXIT_CODE_ERROR_RESTORE_NO_BACKUPS_AVAILABLE```   | No backups to restore are available in internal/external sotrage |
| 11        | ```EXIT_CODE_ERROR_ROOM_DATABASE_MISSING```          | No room database to backup is provided |
| 12        | ```EXIT_CODE_ERROR_STORAGE_PERMISSONS_NOT_GRANTED``` | Storage permissions not granted for custom dialog |
| 13        | ```EXIT_CODE_ERROR_WRONG_DECRYPTION_PASSWORD```      | Cannot decrypt provided backup file because the password is incorrect |

### Example Activity (Kotlin and Java)

#### Kotlin

* ##### Backup

    ```kotlin
        val backup = RoomBackup(this)
        ...
        backup
            .database(FruitDatabase.getInstance(this))
            .enableLogDebug(true)
            .backupIsEncrypted(true)
            .customEncryptPassword("YOUR_SECRET_PASSWORD")
            .backupLocation(RoomBackup.BACKUP_FILE_LOCATION_INTERNAL)
            .maxFileCount(5)
            .apply {
                onCompleteListener { success, message, exitCode ->
                    Log.d(TAG, "success: $success, message: $message, exitCode: $exitCode")
                    if (success) restartApp(Intent(this@MainActivity, MainActivity::class.java))
                }
            }
            .backup()
    ```

* ##### Restore

    ```kotlin
        val backup = RoomBackup(this)
        ...
        backup
            .database(FruitDatabase.getInstance(this))
            .enableLogDebug(true)
            .backupIsEncrypted(true)
            .customEncryptPassword("YOUR_SECRET_PASSWORD")
            .backupLocation(RoomBackup.BACKUP_FILE_LOCATION_INTERNAL)
            .apply {
                onCompleteListener { success, message, exitCode ->
                    Log.d(TAG, "success: $success, message: $message, exitCode: $exitCode")
                    if (success) restartApp(Intent(this@MainActivity, MainActivity::class.java))
                }
            }
            .restore()
    ```

#### Java

* ##### Backup

    ```java
    final RoomBackup roomBackup = new RoomBackup(MainActivityJava.this);
    ...
    roomBackup.database(FruitDatabase.Companion.getInstance(getApplicationContext()));
    roomBackup.enableLogDebug(enableLog);
    roomBackup.backupIsEncrypted(encryptBackup);
    roomBackup.backupLocation(RoomBackup.BACKUP_FILE_LOCATION_INTERNAL);
    roomBackup.maxFileCount(5);
    roomBackup.onCompleteListener((success, message, exitCode) -> {
        Log.d(TAG, "success: " + success + ", message: " + message + ", exitCode: " + exitCode);
        if (success) roomBackup.restartApp(new Intent(getApplicationContext(), MainActivityJava.class));
    });
    roomBackup.backup();
    ```

* ##### Restore

    ```java
    final RoomBackup roomBackup = new RoomBackup(MainActivityJava.this);
    ...
    roomBackup.database(FruitDatabase.Companion.getInstance(getApplicationContext()));
    roomBackup.enableLogDebug(enableLog);
    roomBackup.backupIsEncrypted(encryptBackup);
    roomBackup.backupLocation(RoomBackup.BACKUP_FILE_LOCATION_INTERNAL);
    roomBackup.onCompleteListener((success, message, exitCode) -> {
        Log.d(TAG, "success: " + success + ", message: " + message + ", exitCode: " + exitCode);
        if (success) roomBackup.restartApp(new Intent(getApplicationContext(), MainActivityJava.class));
    });
    roomBackup.restore();
    ```

### Example Fragment (Kotlin and Java)

##### Kotlin

[`FragmentActivity.kt`](app/src/main/java/de/raphaelebner/roomdatabasebackup/sample/FragmentActivity.kt)  
[`MainFragment.kt`](app/src/main/java/de/raphaelebner/roomdatabasebackup/sample/MainFragment.kt)

##### Java

[`FragmentActivityJava.java`](app/src/main/java/de/raphaelebner/roomdatabasebackup/sample/FragmentActivityJava.java)  
[`MainFragmentJava.java`](app/src/main/java/de/raphaelebner/roomdatabasebackup/sample/MainFragmentJava.java)


Sample app
----------

1. Download this repo
2. Unzip
3. Android Studio --> File --> Open --> select this Project
4. within the app folder you find the sample app

Developed by
----------

* Raphael Ebner
* [paypal.me/raphaelebner](https://www.paypal.me/raphaelebner)



License
----------

    MIT License

    Copyright (c) 2024 Raphael Ebner

    Permission is hereby granted, free of charge, to any person obtaining a copy
    of this software and associated documentation files (the "Software"), to deal
    in the Software without restriction, including without limitation the rights
    to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
    copies of the Software, and to permit persons to whom the Software is
    furnished to do so, subject to the following conditions:

    The above copyright notice and this permission notice shall be included in all
    copies or substantial portions of the Software.

    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
    IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
    AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
    LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
    OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
    SOFTWARE.
