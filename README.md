# Android-Room-Database-Backup


![Build](https://github.com/rafi0101/Android-Room-Database-Backup/workflows/Android%20CI/badge.svg)
[![](https://jitpack.io/v/rafi0101/Android-Room-Database-Backup.svg)](https://jitpack.io/#rafi0101/Android-Room-Database-Backup)
[![API](https://img.shields.io/badge/API-21%2B-brightgreen.svg?style=flat)](https://android-arsenal.com/api?level=21)
![Language](https://img.shields.io/badge/language-Kotlin-orange.svg)
[![PRWelcome](https://img.shields.io/badge/PRs-welcome-brightgreen.svg)](https://github.com/rafi0101/Android-Room-Database-Backup/pulls)

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
* Save the backup to internal (private) or external (public) app storage
* Material design
* Written in Kotlin

Content
-----------
* [Features](#Features)
* [Getting started](#Getting-started)
* [Usage](#Usage)
* [Sample app](#Sample-app)
* [Developed by](#Developed-by)
* [License](#License)

[Changelog](CHANGELOG.md)

 Getting started
-----------
Add the JitPack repository to your project-level `build.gradle` file.

```groovy
allprojects {
  repositories {
    // your other repositories ...
    maven { url 'https://jitpack.io' }
  }
}
```

Then, add the dependency for `Android-Room-Database-Backup ` to your app-level `build.gradle` file.

```groovy
implementation 'com.github.rafi0101:Android-Room-Database-Backup:1.0.0-beta07'
```

 Usage
-----------

* [Properties](#Properties)
* [Example Kotlin](#Example-Kotlin)
* [Example Java](#Example-Java)

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

e.g. [`YourDatabase.kt`](app/src/main/java/com/ebner/roomdatabasebackup/sample/database/main/FruitDatabase.kt)

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
      * Custom
          * You can choose to save or restore where ever you want. A CreateDocument() or OpenDocument() Activity will be launched where you can choose the location
          * If your backup is encrypted I reccomend you using a custom encrption password else you can't restore your backup  
          * ```RoomBackup.BACKUP_FILE_LOCATION_CUSTOM_DIALOG```


    ```kotlin
    .backupLocation(RoomBackup.BACKUP_FILE_LOCATION_INTERNAL)
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
    .onCompleteListener { success, message ->
    }
    ```
    
  * Restart your Application. Can be implemented in the onCompleteListener, when "success == true"
      
      **Attention**\
      it does not always work reliably!\
      But you can use other methods.\
      Important is that all activities / fragments that are still open must be closed and reopened\
      Because the Database instance is a new one, and the old activities / fragments are trying to work with the old instance
      
      
    ```kotlin
    .restartApp(Intent(this@MainActivity, MainActivity::class.java))
    ```

### Example Kotlin

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
                onCompleteListener { success, message ->
                    Log.d(TAG, "success: $success, message: $message")
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
                onCompleteListener { success, message ->
                    Log.d(TAG, "success: $success, message: $message")
                    if (success) restartApp(Intent(this@MainActivity, MainActivity::class.java))
                }
        
            }
            .restore()
    ```

### Example Java

* ##### Backup
    
    ```java
    final RoomBackup roomBackup = new RoomBackup(MainActivityJava.this);
    ...
    roomBackup.database(FruitDatabase.Companion.getInstance(getApplicationContext()));
    roomBackup.enableLogDebug(enableLog);
    roomBackup.backupIsEncrypted(encryptBackup);
    roomBackup.backupLocation(RoomBackup.BACKUP_FILE_LOCATION_INTERNAL);
    roomBackup.maxFileCount(5);
    roomBackup.onCompleteListener((success, message) -> {
        Log.d(TAG, "success: " + success + ", message: " + message);
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
    roomBackup.onCompleteListener((success, message) -> {
        Log.d(TAG, "success: " + success + ", message: " + message);
        if (success) roomBackup.restartApp(new Intent(getApplicationContext(), MainActivityJava.class));
    });
    roomBackup.restore();
    ```

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

    Copyright (c) 2021 Raphael Ebner

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
