package io.kang.akka.iot.part5;

import akka.actor.testkit.typed.javadsl.TestKitJunitResource;
import akka.actor.testkit.typed.javadsl.TestProbe;
import akka.actor.typed.ActorRef;
import io.kang.akka.iot.part3.Device;
import io.kang.akka.iot.part4.DeviceManager;
import org.junit.ClassRule;
import org.junit.Test;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static junit.framework.TestCase.assertEquals;

public class DeviceGroupQueryTest {
    @ClassRule
    public static final TestKitJunitResource testKit = new TestKitJunitResource();

    @Test
    public void testReturnTemperatureValueForWorkingDevices() {
        TestProbe<DeviceManager.RespondAllTemperatures> requester =
                testKit.createTestProbe(DeviceManager.RespondAllTemperatures.class);
        TestProbe<Device.Command> device1 = testKit.createTestProbe(Device.Command.class);
        TestProbe<Device.Command> device2 = testKit.createTestProbe(Device.Command.class);

        Map<String, ActorRef<Device.Command>> deviceIdToActor = new HashMap<>();
        deviceIdToActor.put("device1", device1.getRef());
        deviceIdToActor.put("device2", device2.getRef());

        ActorRef<DeviceGroupQuery.Command> queryActor =
                testKit.spawn(
                        DeviceGroupQuery.create(
                                deviceIdToActor, 1L, requester.getRef(), Duration.ofSeconds(3)));

        device1.expectMessageClass(Device.ReadTemperature.class);
        device2.expectMessageClass(Device.ReadTemperature.class);

        queryActor.tell(
                new DeviceGroupQuery.WrappedRespondTemperature(
                        new Device.RespondTemperature(0L, "device1", Optional.of(1.0))));

        queryActor.tell(
                new DeviceGroupQuery.WrappedRespondTemperature(
                        new Device.RespondTemperature(0L, "device2", Optional.of(2.0))));

        DeviceManager.RespondAllTemperatures response = requester.receiveMessage();
        assertEquals(1L, response.requestId);

        Map<String, DeviceManager.TemperatureReading> expectedTemperatures = new HashMap<>();
        expectedTemperatures.put("device1", new DeviceManager.Temperature(1.0));
        expectedTemperatures.put("device2", new DeviceManager.Temperature(2.0));

        assertEquals(expectedTemperatures, response.temperatures);
    }
}
