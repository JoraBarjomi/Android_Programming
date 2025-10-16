import kotlin.random.Random
import kotlin.random.nextInt
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class Driver(_name: String, _age: Int) : Human(_name, _age){

    val dir = Random.nextBoolean()
    val xory = Random.nextBoolean()

    override fun move(){

        val NewThread = Thread{

            runBlocking {
                mutex.withLock {
                    var intdir = 0;
                    if(dir){
                        intdir = 1;
                    } else{
                        intdir = -1;
                    }

                    if(xory){
                        val rnd = velocity * velocity * intdir
                        x += rnd
                    } else{
                        val rnd2 = velocity * velocity * intdir
                        y += rnd2
                    }
                }
            }

            println("Driver $name rides {$x, $y}")

        }

        NewThread.start()

    }
}