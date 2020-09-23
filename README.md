# Android-Room-Database-Backup


[![](https://jitpack.io/v/rafi0101/Android-Room-Database-Backup.svg)](https://jitpack.io/#rafi0101/Android-Room-Database-Backup)
[![API](https://img.shields.io/badge/API-26%2B-brightgreen.svg?style=flat)](https://android-arsenal.com/api?level=26)
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

Then, add the dependency for `Android-Room-Database-Backup ` to your project-level `build.gradle` file.

```groovy
implementation 'com.github.rafi0101:Android-Room-Database-Backup:1.0.0-beta04'
```

 Usage
-----------

* [Properties](#Properties)
* [Example Kotlin](#Example-Kotlin)
* [Example Java](#Example-Java)

### Properties

**Required**

  * Current context


    ```kotlin
    .context(this)
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
      
      **Attention**\
      i do not assume any liability for the loss of your key
      
      
    ```kotlin
    .customEncryptPassword("YOUR_SECRET_PASSWORD")
    ```
    
    
  * Save your backup to external app storage
      * External
          * storage path: /storage/emulated/0/Android/data/*package*/files/backup/
          * This files will be deleted, if you uninstall your app
      * Internal
          * Private, storage not accessible
          * This files will be deleted, if you uninstall your app
    

    ```kotlin
    .useExternalStorage(false)
    ```
    
  * Set a custom dialog title, when showing list of available backups to restore
    
    
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
        RoomBackup()
            .context(this)
            .database(FruitDatabase.getInstance(this))
            .enableLogDebug(true)
            .backupIsEncrypted(true)
            .useExternalStorage(false)
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
        RoomBackup()
            .context(this)
            .database(FruitDatabase.getInstance(this))
            .enableLogDebug(true)
            .backupIsEncrypted(true)
            .useExternalStorage(false)
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
    final RoomBackup roomBackup = new RoomBackup();
    roomBackup.context(MainActivityJava.this);
    roomBackup.database(FruitDatabase.Companion.getInstance(getApplicationContext()));
    roomBackup.enableLogDebug(enableLog);
    roomBackup.backupIsEncrypted(encryptBackup);
    roomBackup.useExternalStorage(useExternalStorage);
    roomBackup.maxFileCount(5);
    roomBackup.onCompleteListener(new OnCompleteListener() {
        @Override
        public void onComplete(boolean success, @NotNull String message) {
            Log.d(TAG, "success: " + success + ", message: " + message);
            if (success) roomBackup.restartApp(new Intent(getApplicationContext(), MainActivityJava.class));
        }
    });
    roomBackup.backup();
    ```
    
* ##### Restore
    
    ```java
    final RoomBackup roomBackup = new RoomBackup();
    roomBackup.context(MainActivityJava.this);
    roomBackup.database(FruitDatabase.Companion.getInstance(getApplicationContext()));
    roomBackup.enableLogDebug(enableLog);
    roomBackup.backupIsEncrypted(encryptBackup);
    roomBackup.useExternalStorage(useExternalStorage);
    roomBackup.onCompleteListener(new OnCompleteListener() {
        @Override
        public void onComplete(boolean success, @NotNull String message) {
            Log.d(TAG, "success: " + success + ", message: " + message);
            if (success) roomBackup.restartApp(new Intent(getApplicationContext(), MainActivityJava.class));
        }
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

    Copyright 2020 Raphael Ebner
    
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
    
        http://www.apache.org/licenses/LICENSE-2.0
    
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.