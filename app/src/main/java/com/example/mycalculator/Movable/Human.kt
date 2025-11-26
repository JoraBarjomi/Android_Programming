import kotlin.random.Random
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

open class Human : Movable{

    protected val mutex = Mutex()

    var name: String = ""
    var age: Int = 0
    override val velocity: Int = Random.nextInt(0, 5)

    override var x : Int = 0
    override var y : Int = 0

    constructor(_name: String, _age: Int){
        name = _name
        age = _age
    }

    override fun move(){

        val NewThread = Thread{

            runBlocking {
                mutex.withLock {
                    val rnd = velocity * Random.nextInt(-1, 2)
                    val rnd2 = velocity * Random.nextInt(-1, 2)

                    x += rnd
                    y += rnd2
                }
            }

            println("$name is moved {$x, $y}")

        }
        NewThread.start()

    }
}