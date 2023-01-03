package app.codinguyy.coroutineexample

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import app.codinguyy.coroutineexample.databinding.FragmentFirstBinding
import kotlinx.coroutines.*
import kotlin.system.measureTimeMillis

// https://www.youtube.com/watch?v=t-3TOke8tq8

class FirstFragment : Fragment() {

    /**
     * Coroutine Builder:
     * launch creates a StandAloneCoroutine,
     * async creates a DeferredCoroutine,
     * runBlocking creates a BlockingCoroutine
     *
     * Scopes: GlobalScope, ViewModelScope, CoroutineScope
     *
     */

    private var _binding: FragmentFirstBinding? = null
    private val viewModel: FirstFragmentViewModel by viewModels()

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonFirst.setOnClickListener {
            findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
        }

        // suspend function

        // switch threads
        // wait for response
        // how to cancel a coroutine
        // get results out of couroutine
        // Cancelation and Exceptions
        // runBlocking

        /**
         * Example 1: Chaning network calls

         * The executiontime is 6s here
         * The output of the first call should be the input
         * to the second call
         */
        CoroutineScope(Dispatchers.IO).launch {
            val time = measureTimeMillis {
                val answer1 = netWorkCall1()
                val answer2 = networkCall2()

                Log.i("answer", answer1)
                Log.i("answer", answer2)
            }
            Log.i("answer", "Request took $time ms.")
        }

        /**
         * Example 2: Bad practice
         */

        CoroutineScope(Dispatchers.IO).launch {
            val time = measureTimeMillis {
                var answer1: String? = null
                var answer2: String? = null
                val job1 = launch { answer1 = netWorkCall1() }
                val job2 = launch { answer2 = networkCall2() }
                job1.join()
                job2.join()
            }
        }

        /**
         * Example 3: Do work simultan
         * async and await
         * The execution time is 3s here,
         * because we are executing the jobs simultaniously.
         */

        CoroutineScope(Dispatchers.IO).launch {
            val time = measureTimeMillis {
                val answer1 = async {
                    netWorkCall1()
                }
                val answer2 = async {
                    networkCall2()
                }

                Log.d("answer", answer1.await())
                Log.d("answer", answer2.await())

                Log.d(
                    "answer",
                    Thread.currentThread().name.toString()
                )
            }
        }

        /**
         * Example 4: async
         */

        CoroutineScope(Dispatchers.IO).launch {
            val jobDeffered: Deferred<String> = async {
                "foo"
            }
            // Log.i("answer",jobDeffered.await())
            jobDeffered.join()
        }

        /**
         * Example 5: When to use GlobalScope or CoroutineScope?
         */
        GlobalScope.launch(Dispatchers.IO) {
            // playing music
            // download file
            // -> Use carefully
        }
        CoroutineScope(Dispatchers.IO).launch {
            // data computation
        }

        /**
         * Example 6: Creating Couroutine Jobs, start or cancel
         *
         */

        CoroutineScope(Dispatchers.IO).launch {
            coroutineJob().join()
            coroutineJob().cancel()
        }

        /**
         * Example 8: Cancel coroutine - delay method - Same description as in Example 9
         */

        runBlocking {
            val job = launch {
                for (i in 0..500) {
                    Log.i("", "$i")
                    delay(50)
                }
            }
            delay(200)
            job.cancel()
            // job.cancelAndJoin()
        }

        /**
         * Example 9: Cancel coroutine - yield() - Make the coroutine cooperative with the
         * yield() method. So we are able to cancel the coroutine
         */

        runBlocking {
            val job = launch {
                for (i in 0..500) {
                    Log.i("", "$i")
                    // yield()
                }
            }
            delay(10)
            job.cancel()
        }

        /**
         * Example 9: Cancel coroutine -isActive, return@launch
         * Make the coroutine cooperative with the boolean isActive
         */

        runBlocking {
            val job = launch {
                for (i in 0..500) {
                    Log.i("", "$i")
                    if (!isActive) {
                        return@launch
                    }
                }
            }
            delay(10)
            job.cancel()
        }

        /**
         * Example 10: Cancel coroutine - try and catch
         */

        runBlocking {
            val job = launch {
                try {
                    for (i in 0..500) {
                        delay(5) // or yield() - delay() and yield() are cancelable functions
                        Log.i("", "$i")
                    }
                } catch (e: CancellationException) {
                    Log.i("answer", "Catch exception safely")
                } finally {
                    // no suspend functions here or
                    withContext(NonCancellable) {
                        delay(1000)
                        Log.i("answer", "Close resource finally")
                    }
                }
            }
            delay(10)
            job.cancel()
        }

        /**
         * Example 10: Cancel coroutine - Exception message
         */

        runBlocking {
            val job = launch {
                try {
                    for (i in 0..500) {
                        delay(5) // or yield() - delay() and yield() are cancelable functions
                        Log.i("", "$i")
                    }
                } catch (e: CancellationException) {
                    Log.i("answer", e.message.toString())
                } finally {
                    // no suspend functions here or
                    withContext(NonCancellable) {
                        delay(1000)
                        Log.i("answer", "Close resource finally")
                    }
                }
            }
            delay(10)
            job.cancel("My own crash exception")
        }

        /**
         * Example 11: Cancel coroutine - withTimeout()
         */
        runBlocking {
            withTimeout(100L) {
                try {
                    for (i in 0..500) {
                        Log.i("", "$i")
                    }
                } catch (e: TimeoutCancellationException) {
                    // code
                } finally {
                    // code
                }
            }
        }

        /**
         * Example 12: Cancel coroutine - withTimeoutOrNull()
         * When the coroutine is not done within 100 ms, the result of the
         * coroutine is null
         */
        val result: String = runBlocking {
            withTimeoutOrNull(1000L) {
                try {
                    for (i in 0..500) {
                        Log.i("", "$i")
                    }
                } catch (e: TimeoutCancellationException) {
                    // code
                } finally {
                    // code
                }
            }
            "The coroutine is over and will be assigned to the variable result"
        }

        /**
         * Example 13: Cancel coroutine - Start Coroutine lazyly
         * the Coroutine will only be started, when the variable msgOne will be used
         *
         */

        CoroutineScope(Dispatchers.IO).launch {
            val msgOne = async(start = CoroutineStart.LAZY) { getMessage() }
            val msgTwo = async(start = CoroutineStart.LAZY) { getMessage() }
            Log.i("answer", msgOne.await() + "" + msgTwo.await())
        }


        /**
         * Example 14: CoroutineContext
         */

        CoroutineScope(Dispatchers.Main).launch { //MainThread

            launch { //MainThread, this Coroutine is a child of the parent Coroutine, which
                        // is happening on the main thread

            }

        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /**
     * suspend keyword is needed, because the delay method
     * is only allowed in a coroutine.
     * With the suspend keyword I am ensuring to the delay
     * method, that this method is only called from a coroutine
     */

    suspend fun netWorkCall1(): String {
        delay(3000L)
        return "answer 1"
    }

    suspend fun networkCall2(): String {
        delay(3000L)
        return "Answer 2"
    }

    suspend fun getMessage(): String {
        return "messageOne"
    }

    fun coroutineJob() = CoroutineScope(Dispatchers.IO).launch {
        delay(1000L)
    }
}
