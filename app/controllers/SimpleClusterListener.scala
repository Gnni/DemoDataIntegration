package controllers

import akka.actor.{Actor, ActorLogging}
import akka.cluster.Cluster
import akka.cluster.ClusterEvent._
import play.api.Logger

/**
	* Created by guenterhesse on 16/08/16.
	*/
class SimpleClusterListener extends Actor with ActorLogging {

	val cluster = Cluster(context.system)

	//subscribe to cluster changes, re-subscribe when restart
	override def preStart(): Unit = {
		Logger.info(s"pre start in progress, system's name: ${context.system.name}")
		cluster.subscribe(self, initialStateMode = InitialStateAsEvents,
			classOf[MemberEvent], classOf[UnreachableMember])
	}

	override def postStop(): Unit = cluster.unsubscribe(self)

	def receive = {
		case MemberUp(member) =>
			//log.info("Member is Up: {}", member.address)
			Logger.info(s"Member is Up: ${member.address}")
		case UnreachableMember(member) =>
			//log.info("Member detected as unreachable: {}", member)
			Logger.info(s"Member detected as unreachable: $member")
		case MemberRemoved(member, previousStatus) =>
			//log.info("Member is Removed: {} after {}",
			//	member.address, previousStatus)
			Logger.info(s"Member is Removed: ${member.address} after $previousStatus")
		case _: MemberEvent => Logger.info("we ignore...")// ignore
		case msg: String => Logger.info(s"got message!!$msg")
	}

}
