import kotlin.random.Random
import kotlin.random.nextInt
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

open class Human{

    protected val mutex = Mutex()

    var name: String = ""
    var age: Int = 0
    val velocity: Int = Random.nextInt(0, 5)

    var x : Int = 0
    var y : Int = 0

    constructor(_name: String, _age: Int){
        name = _name
        age = _age
    }

    open fun move(){

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

fun main() {

    var oleg : Human = Human("Oleg Ivanov", 20)
    oleg.move()

    val people = mutableListOf<Human>()

    var alex : Driver = Driver("Alex", 25)
    for (i in 1..4){
        people.add(Human("Human$i", Random.nextInt(1, 100)))
    }

    val second : Int = 10

    for(i in 1..second){
        for(person in people){
            person.move()
        }
        alex.move()
    }

}