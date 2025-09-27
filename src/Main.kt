import kotlin.random.Random
import kotlin.random.nextInt
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

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