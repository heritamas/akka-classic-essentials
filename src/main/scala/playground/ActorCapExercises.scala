package playground

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import part2actors.ActorCapabilities.{Counter, system}

object ActorCapExercises extends App {
  /**
   * Exercises
   *
   * 1. a Counter actor
   *   - Increment
   *   - Decrement
   *   - Print
   *
   * 2. a Bank account as an actor
   *   receives
   *   - Deposit an amount
   *   - Withdraw an amount
   *   - Statement
   *   replies with
   *   - Success
   *   - Failure
   *
   *   interact with some other kind of actor
   */
  val system = ActorSystem("actorCapabilitiesDemo")

  println("Counter")

  object CounterMessage {
    case object Increment
    case object Decrement
    case object Print
  }

  class Counter extends Actor {
    import CounterMessage._
    var count = 0

    override def receive: Receive = {
      case Increment => count += 1
      case Decrement => count -= 1
      case Print => println(s"[counter] My current count is $count")
    }
  }

  val counter = system.actorOf(Props[Counter], "myCounter")

  (1 to 5).foreach(_ => counter ! Counter.Increment)
  (1 to 3).foreach(_ => counter ! Counter.Decrement)
  counter ! Counter.Print

  println("Banking System")

  object BankAccountMessage {
    case class Deposit(amount: Int)
    case class Withdraw(amount: Int)
    case object Statement

    case class TransactionSuccess(message: String)
    case class TransactionFailure(reason: String)

  }

  class BankAccount extends Actor {
    import BankAccountMessage._
    private var balance = 0

    override def receive: Receive =  {
      case Deposit(amount) => {
        balance += amount
        sender() ! TransactionSuccess(s"Successfully deposited $amount")
      }
      case Withdraw(amount) => {
        if (balance >= amount) {
          balance -= amount
          sender() ! TransactionSuccess(s"Successfully withdrew $amount")
        } else {
          sender() ! TransactionFailure(s"Insufficient funds to withdraw $amount")
        }
      }
      case Statement => {
        sender() ! TransactionSuccess(s"Your balance is $balance")
      }
    }
  }

  class BankingSystem(val bankAccount: ActorRef) extends Actor {
    import BankAccountMessage._

    private def run(): Unit = {
      bankAccount ! Deposit(100)
      bankAccount ! Withdraw(50)
      bankAccount ! Withdraw(500)
      bankAccount ! Statement
    }

    override def receive: Receive = {
      case "run" => run()
      case message => println(s"BankingSystem received message: $message")
    }
  }

  object BankingSystem {
    def props(bankAccount: ActorRef): Props = Props(new BankingSystem(bankAccount))
  }

  val bankAccount = system.actorOf(Props[BankAccount], "myBankAccount")
  val bankingSystem = system.actorOf(BankingSystem.props(bankAccount), "myBankingSystem")

  bankingSystem ! "run"
}
