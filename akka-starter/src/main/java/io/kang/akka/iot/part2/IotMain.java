package io.kang.akka.iot.part2;

import akka.actor.typed.ActorSystem;

public class IotMain {

    public static void main(String[] args) {
        // Create ActorSystem and top level supervisor
        ActorSystem.create(IotSupervisor.create(), "iot-system");
    }
}
