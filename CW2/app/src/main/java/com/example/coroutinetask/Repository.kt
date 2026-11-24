
package com.example.coroutinetask

class Repository {

    fun doHardWork(onProgress: (Int) -> Unit) {

        for (i in 1..10) {

            Thread.sleep(500)

            onProgress(i)
        }
    }
}
