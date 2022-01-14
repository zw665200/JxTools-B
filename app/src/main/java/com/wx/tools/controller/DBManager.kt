package com.wx.tools.controller

import android.content.Context
import com.wx.tools.bean.*
import com.wx.tools.model.db.AppDatabase
import com.wx.tools.utils.JLog

object DBManager {

    fun insert(context: Context, account: Account) {
        val dao = AppDatabase.getDatabase(context).accountDao()
        val acc = dao.find(account.id)
        if (acc != null) {
            dao.update(account)
        } else {
            dao.insert(account)
        }
    }

    fun insert(context: Context, talker: Talker) {
        val dao = AppDatabase.getDatabase(context).talkerDao()
        val acc = dao.findTalkersById(talker.id)
        if (acc != null && acc.isNotEmpty()) {
            dao.update(talker)
        } else {
            dao.insert(talker)
        }
    }

    fun insert(context: Context, contact: Contact) {
        val dao = AppDatabase.getDatabase(context).contactDao()
        val acc = dao.findContact(contact.id)
        if (acc != null) {
            dao.update(contact)
        } else {
            dao.insert(contact)
        }
    }

    fun insert(context: Context, message: Message) {
        val dao = AppDatabase.getDatabase(context).messageDao()
        val acc = dao.findMessageById(message.id)
        if (acc != null) {
            dao.update(message)
        } else {
            dao.insert(message)
        }
    }

    fun insert(context: Context, file: FileWithType) {
        val dao = AppDatabase.getDatabase(context).fileDao()
        val acc = dao.find(file.path)
        if (acc != null) {
            dao.update(file)
        } else {
            dao.insert(file)
        }
    }

    /**
     * 插入PayData数据
     */
    fun insert(context: Context, file: PayData) {
        val dao = AppDatabase.getDatabase(context).payDao()
        val acc = dao.find(file.name)
        if (acc != null) {
            dao.update(file)
        } else {
            dao.insert(file)
        }
    }

    /**
     * insert ServicePrice
     */
    fun insert(context: Context, file: Price) {
        val dao = AppDatabase.getDatabase(context).priceDao()
        val acc = dao.find(file.id)
        if (acc != null) {
            dao.update(file)
        } else {
            dao.insert(file)
        }
    }

    fun getAllAccount(context: Context): List<Account>? {
        val dao = AppDatabase.getDatabase(context).accountDao()
        return dao.getAll()
    }


    fun getAllTalker(context: Context): List<Talker>? {
        val dao = AppDatabase.getDatabase(context).talkerDao()
        return dao.getAll()
    }


    fun getAccountsBySrcTime(context: Context, srcTime: Long): List<Account>? {
        val dao = AppDatabase.getDatabase(context).accountDao()
        return dao.findAccountBySrcPath(srcTime)
    }


    fun getTalkers(context: Context, accountName: String, srcTime: Long, pageIndex: Int): ArrayList<Talker> {
        val dao = AppDatabase.getDatabase(context).talkerDao()
        val talkers = dao.findTalkersByAccount(accountName, srcTime, pageIndex)
        if (talkers.isNullOrEmpty()) {
            return arrayListOf()
        }
        return talkers as ArrayList<Talker>
    }


    fun getContacts(context: Context, accountName: String, srcTime: Long): ArrayList<Contact> {
        val dao = AppDatabase.getDatabase(context).contactDao()
        val contacts = dao.findContacts(accountName, srcTime)
        if (contacts.isNullOrEmpty()) {
            return arrayListOf()
        }
        return contacts as ArrayList<Contact>
    }

    fun getGroupContacts(context: Context, accountName: String, srcTime: Long): ArrayList<Contact> {
        val dao = AppDatabase.getDatabase(context).contactDao()
        val contacts = dao.findGroupContacts(accountName, srcTime)
        if (contacts.isNullOrEmpty()) {
            return arrayListOf()
        }
        return contacts as ArrayList<Contact>
    }

    fun getNotFriendInGroupContacts(context: Context, accountName: String, srcTime: Long): ArrayList<Contact> {
        val dao = AppDatabase.getDatabase(context).contactDao()
        val contacts = dao.findNotFriendInGroupContacts(accountName, srcTime)
        if (contacts.isNullOrEmpty()) {
            return arrayListOf()
        }
        return contacts as ArrayList<Contact>
    }

    fun getGhContacts(context: Context, accountName: String, srcTime: Long): ArrayList<Contact> {
        val dao = AppDatabase.getDatabase(context).contactDao()
        val contacts = dao.findGhContacts(accountName, srcTime)
        if (contacts.isNullOrEmpty()) {
            return arrayListOf()
        }
        return contacts as ArrayList<Contact>
    }


    fun getMessages(context: Context, accountName: String, talkerName: String, limit: Int, offset: Int): ArrayList<Message> {
        val dao = AppDatabase.getDatabase(context).messageDao()
        val talkers = dao.findMessagesByName(accountName, talkerName, limit, offset)
        return if (talkers.isNullOrEmpty()) {
            arrayListOf()
        } else {
            talkers as ArrayList<Message>
        }
    }


    fun getAllFiles(context: Context): ArrayList<FileWithType> {
        val dao = AppDatabase.getDatabase(context).fileDao()
        val list = dao.getAll()
        if (list.isNullOrEmpty()) {
            return arrayListOf()
        }

        return list as ArrayList<FileWithType>
    }

    fun getPicByKey(context: Context, type: String, minSize: Long, maxSize: Long, minDate: Long, maxDate: Long): ArrayList<FileWithType> {
        val dao = AppDatabase.getDatabase(context).fileDao()
        val result = arrayListOf<FileWithType>()
        when (type) {
            "qq" -> {
                JLog.i("$type+$minSize+$maxSize+$minDate+$maxDate")
                val list = dao.find(minSize, maxSize, minDate, maxDate) as ArrayList<FileWithType>
                for (child in list) {
                    if (child.type.startsWith("qq_")) {
                        result.add(child)
                    }
                }
            }

            "wx" -> {
                JLog.i("$type+$minSize+$maxSize+$minDate+$maxDate")
                val list = dao.find(minSize, maxSize, minDate, maxDate) as ArrayList<FileWithType>
                for (child in list) {
                    if (child.type.startsWith("wx_")) {
                        result.add(child)
                    }
                }
            }

            else -> {
                JLog.i("$type+$minSize+$maxSize+$minDate+$maxDate")
                return dao.find(type, minSize, maxSize, minDate, maxDate) as ArrayList<FileWithType>
            }
        }

        if (result.isNullOrEmpty()) {
            return arrayListOf()
        }

        return result
    }

    fun getVoiceByKey(context: Context, sort: String, minSize: Long, maxSize: Long, minDate: Long, maxDate: Long): ArrayList<FileWithType> {
        val dao = AppDatabase.getDatabase(context).fileDao()
        val result = arrayListOf<FileWithType>()
        JLog.i("$sort+$minSize+$maxSize+$minDate+$maxDate")
        when (sort) {
            "default" -> {
                val list = dao.findVoices(minSize, maxSize, minDate, maxDate) as ArrayList<FileWithType>
                if (list.isNotEmpty()) {
                    result.addAll(list)
                }
            }

            "date_desc" -> {
                val list = dao.findVoicesByDateDesc(minSize, maxSize, minDate, maxDate) as ArrayList<FileWithType>
                if (list.isNotEmpty()) {
                    result.addAll(list)
                }
            }

            "date_asc" -> {
                val list = dao.findVoicesByDateDesc(minSize, maxSize, minDate, maxDate) as ArrayList<FileWithType>
                if (list.isNotEmpty()) {
                    result.addAll(list)
                }
            }

            "size_desc" -> {
                val list = dao.findVoicesBySizeDesc(minSize, maxSize, minDate, maxDate) as ArrayList<FileWithType>
                if (list.isNotEmpty()) {
                    result.addAll(list)
                }
            }

            "size_asc" -> {
                val list = dao.findVoicesBySizeDesc(minSize, maxSize, minDate, maxDate) as ArrayList<FileWithType>
                if (list.isNotEmpty()) {
                    result.addAll(list)
                }
            }
        }

        if (result.isNullOrEmpty()) {
            return arrayListOf()
        }

        return result
    }


    fun getVideoByKey(context: Context, sort: String, minSize: Long, maxSize: Long, minDate: Long, maxDate: Long): ArrayList<FileWithType> {
        val dao = AppDatabase.getDatabase(context).fileDao()
        val result = arrayListOf<FileWithType>()
        JLog.i("$sort+$minSize+$maxSize+$minDate+$maxDate")
        when (sort) {
            "default" -> {
                val list = dao.findVideos(minSize, maxSize, minDate, maxDate) as ArrayList<FileWithType>
                if (list.isNotEmpty()) {
                    result.addAll(list)
                }
            }

            "date_desc" -> {
                val list = dao.findVideosByDateDesc(minSize, maxSize, minDate, maxDate) as ArrayList<FileWithType>
                if (list.isNotEmpty()) {
                    result.addAll(list)
                }
            }

            "date_asc" -> {
                val list = dao.findVideosByDateAsc(minSize, maxSize, minDate, maxDate) as ArrayList<FileWithType>
                if (list.isNotEmpty()) {
                    result.addAll(list)
                }
            }

            "size_desc" -> {
                val list = dao.findVideosBySizeDesc(minSize, maxSize, minDate, maxDate) as ArrayList<FileWithType>
                if (list.isNotEmpty()) {
                    result.addAll(list)
                }
            }

            "size_asc" -> {
                val list = dao.findVideosBySizeAsc(minSize, maxSize, minDate, maxDate) as ArrayList<FileWithType>
                if (list.isNotEmpty()) {
                    result.addAll(list)
                }
            }
        }

        if (result.isNullOrEmpty()) {
            return arrayListOf()
        }

        return result
    }

    fun getDocByKey(context: Context, sort: String, minSize: Long, maxSize: Long, minDate: Long, maxDate: Long): ArrayList<FileWithType> {
        val dao = AppDatabase.getDatabase(context).fileDao()
        val result = arrayListOf<FileWithType>()
        JLog.i("$sort+$minSize+$maxSize+$minDate+$maxDate")
        when (sort) {
            "default" -> {
                val list = dao.findDocs(minSize, maxSize, minDate, maxDate) as ArrayList<FileWithType>
                if (list.isNotEmpty()) {
                    result.addAll(list)
                }
            }

            "date_desc" -> {
                val list = dao.findDocsByDateDesc(minSize, maxSize, minDate, maxDate) as ArrayList<FileWithType>
                if (list.isNotEmpty()) {
                    result.addAll(list)
                }
            }

            "date_asc" -> {
                val list = dao.findDocsByDateDesc(minSize, maxSize, minDate, maxDate) as ArrayList<FileWithType>
                if (list.isNotEmpty()) {
                    result.addAll(list)
                }
            }

            "size_desc" -> {
                val list = dao.findDocsBySizeDesc(minSize, maxSize, minDate, maxDate) as ArrayList<FileWithType>
                if (list.isNotEmpty()) {
                    result.addAll(list)
                }
            }

            "size_asc" -> {
                val list = dao.findDocsBySizeDesc(minSize, maxSize, minDate, maxDate) as ArrayList<FileWithType>
                if (list.isNotEmpty()) {
                    result.addAll(list)
                }
            }
        }

        if (result.isNullOrEmpty()) {
            return arrayListOf()
        }

        return result
    }


    /**
     * 获取所有的支付数据
     */
    fun getPayData(context: Context): ArrayList<PayData> {
        val dao = AppDatabase.getDatabase(context).payDao()
        val list = dao.getAll()
        if (list.isNullOrEmpty()) {
            return arrayListOf()
        }

        return list as ArrayList<PayData>
    }

    /**
     * 通过key查找支付数据
     */
    fun getPayDataByKey(context: Context, time: String): PayData? {
        val dao = AppDatabase.getDatabase(context).payDao()
        return dao.find(time)
    }

    /**
     * 通过key查找支付数据
     */
    fun getPayDataByKey(context: Context, time: String, isMenu: Boolean): PayData? {
        val dao = AppDatabase.getDatabase(context).payDao()
        return dao.find(time, isMenu)
    }

    /**
     * 通过serviceId获得价格
     */
    fun getServicePrice(context: Context, serviceId: Int): Price? {
        val dao = AppDatabase.getDatabase(context).priceDao()
        return dao.find(serviceId)
    }

    /**
     * 删除数据
     */
    fun delete(context: Context, file: FileWithType) {
        val dao = AppDatabase.getDatabase(context).fileDao()
        dao.delete(file)
    }

    /**
     * 删除数据
     */
    fun deleteFiles(context: Context) {
        val dao = AppDatabase.getDatabase(context).fileDao()
        dao.delete()

        val accountDao = AppDatabase.getDatabase(context).accountDao()
        val accounts = accountDao.getAll()
        if (!accounts.isNullOrEmpty()) {
            for (account in accounts) {
                accountDao.delete(account)
            }
        }

        val messageDao = AppDatabase.getDatabase(context).messageDao()
        val messages = messageDao.getAll()
        if (!messages.isNullOrEmpty()) {
            for (message in messages) {
                messageDao.delete(message)
            }
        }

    }


}