/*
 * Copyright (c) 2018 Razeware LLC
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * Notwithstanding the foregoing, you may not use, copy, modify, merge, publish,
 * distribute, sublicense, create a derivative work, and/or sell copies of the
 * Software in any work that is designed, intended, or marketed for pedagogical or
 * instructional purposes related to programming, coding, application development,
 * or information technology.  Permission for such use, copying, modification,
 * merger, publication, distribution, sublicensing, creation of derivative works,
 * or sale is expressly withheld.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 *
 */

package com.raywenderlich.android.rwdc2018.repository

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.os.*
import android.util.Log
import com.raywenderlich.android.rwdc2018.app.PhotosUtils

/**
 * AsysncTask
 * Runs code on background thread in doInBackground()
 * Returns a result on the main thread in onPostExecute()
 * Meant for shor-lived background tasks
 *
 * AsyncTask Thread
 * App process only has one thread for use by all AsyncTasks
 * Multiple AsyncTasks will run sequentially on the single thread
 * Using manual threads allows true multithreading, but can lead to race conditions
 */

class PhotosRepository : Repository {
    private val photosLiveData = MutableLiveData<List<String>>()
    private val bannerLiveData = MutableLiveData<String>()

    override fun getPhotos(): LiveData<List<String>> {
        //fetchPhotoData()
        FetchPhotosAsyncTask({ photosLiveData.value = it }).execute()
        return photosLiveData
    }

    override fun getBanner(): LiveData<String> {
//        fetchBannerData()
        FetchBannerAsyncTask({ bannerLiveData.value = it }).execute()
        return bannerLiveData
    }

    private fun fetchPhotoData() {

        //todo handler 사용 방법 확인하기
        //핸들러에 Looper.getMainLooper 를 사용하면 MainThread 로 보낼 수 있다.
        val handler = object : Handler(Looper.getMainLooper()) {
            override fun handleMessage(msg: Message?) {
                val bundle = msg?.data
                photosLiveData.value = bundle?.getStringArrayList("PHOTOS_KEY")
            }
        }

        val runnable = Runnable {
            val photoString = PhotosUtils.photoJsonString()
            Log.i("PhotosRepository", photoString)

            val photos = PhotosUtils.photoUrlsFromJsonString(photoString ?: "")
            if (photos != null) {
                /**
                 * background thread 에서 바로 LiveData 에 업데이트 불가
                 * photosLiveData.value = photos
                 */
                //1. postValue 사용 가능
                //photosLiveData.postValue(photos)

                //2. Handler 사용 가능
                val message = Message()
                val bundle = Bundle()
                bundle.putStringArrayList("PHOTOS_KEY", photos)
                message.data = bundle
                handler.sendMessage(message)
            }
        }
        var thread = Thread(runnable)

        /**
         * thread.start() : 새로운 thread 에서 작업
         * thread.run() : 기존 thread 에서 작업
         */
        thread.start()
    }

    private fun fetchBannerData() {
        //todo handler 사용 방법 확인하기
        //핸들러에 Looper.getMainLooper 를 사용하면 MainThread 로 보낼 수 있다.
        val handler = object : Handler(Looper.getMainLooper()) {
            override fun handleMessage(msg: Message?) {
                val bundle = msg?.data
                bannerLiveData.value = bundle?.getString("BANNER_KEY")
            }
        }

        val runnable = Runnable {
            val photoString = PhotosUtils.photoJsonString()
            Log.i("PhotosRepository", photoString)

            val banners = PhotosUtils.bannerFromJsonString(photoString ?: "")
            if (banners != null) {
                /**
                 * background thread 에서 바로 LiveData 에 업데이트 불가
                 * photosLiveData.value = photos
                 */
                //1. postValue 사용 가능
                //photosLiveData.postValue(photos)

                //2. Handler 사용 가능
                val message = Message()
                val bundle = Bundle()
                bundle.putString("BANNER_KEY", banners)
                message.data = bundle
                handler.sendMessage(message)
            }
        }
        var thread = Thread(runnable)

        /**
         * thread.start() : 새로운 thread 에서 작업
         * thread.run() : 기존 thread 에서 작업
         */
        thread.start()
    }

    class FetchPhotosAsyncTask(var callback: (List<String>) -> Unit) : AsyncTask<Void, Void, List<String>>() {

        override fun doInBackground(vararg params: Void?): List<String>? {
            val photoString = PhotosUtils.photoJsonString()
            Log.i("PhotosRepository", photoString)
            return PhotosUtils.photoUrlsFromJsonString(photoString ?: "")
        }

        override fun onPostExecute(result: List<String>) {
            super.onPostExecute(result)
            callback(result)
        }
    }

    class FetchBannerAsyncTask(var callback : (String) -> Unit) : AsyncTask<Void, Void, String>(){
        override fun doInBackground(vararg params: Void?): String? {
            val photoString = PhotosUtils.photoJsonString()
            Log.i("PhotosRepository", photoString)

            return PhotosUtils.bannerFromJsonString(photoString ?: "")
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            result?.let {
                callback(result)
            }
        }

    }
}