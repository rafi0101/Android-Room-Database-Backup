package com.ebner.roomdatabasebackup.core

/**
 * Created by raphael on 15.06.2020.
 * Android Room Database Backup Created in com.ebner.roomdatabasebackup.core
 */
interface OnCompleteListener {
    fun onComplete(success: Boolean, message: String)
}