package io.kang.akka.iot.part4;

import akka.actor.testkit.typed.javadsl.TestKitJunitResource;
import akka.actor.testkit.typed.javadsl.TestProbe;
import akka.actor.typed.ActorRef;
import io.kang.akka.iot.part3.Device;
import org.junit.ClassRule;
import org.junit.Test;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class DeviceGroupTest {
    @ClassRule
    public static final TestKitJunitResource testKit = new TestKitJunitResource();

    @Test
    public void testReplyToRegistrationRequests() {
        TestProbe<DeviceManager.DeviceRegistered> probe = testKit.createTestProbe(DeviceManager.DeviceRegistered.class);
        ActorRef<DeviceGroup.Command> groupActor = testKit.spawn(DeviceGroup.create("group"));

        groupActor.tell(new DeviceManager.RequestTrackDevice("group", "device", probe.getRef()));
        DeviceManager.DeviceRegistered registered1 = probe.receiveMessage();

        // another deviceId
        groupActor.tell(new DeviceManager.RequestTrackDevice("group", "device3", probe.getRef()));
        DeviceManager.DeviceRegistered registered2 = probe.receiveMessage();
        assertNotEquals(registered1.device, registered2.device);

        // Check that the device actors are working
        TestProbe<Device.TemperatureRecorded> recordProbe =
                testKit.createTestProbe(Device.TemperatureRecorded.class);
        registered1.device.tell(new Device.RecordTemperature(0L, 1.0, recordProbe.getRef()));
        assertEquals(0L, recordProbe.receiveMessage().requestId);
        registered2.device.tell(new Device.RecordTemperature(1L, 2.0, recordProbe.getRef()));
        assertEquals(1L, recordProbe.receiveMessage().requestId);
    }

    @Test
    public void testIgnoreWrongRegistrationRequests() {
        TestProbe<DeviceManager.DeviceRegistered> probe = testKit.createTestProbe(DeviceManager.DeviceRegistered.class);
        ActorRef<DeviceGroup.Command> groupActor = testKit.spawn(DeviceGroup.create("group"));
        groupActor.tell(new DeviceManager.RequestTrackDevice("wrongGroup", "device1", probe.getRef()));
        probe.expectNoMessage();
    }

    @Test
    public void testReturnSameActorForSameDeviceId() {
        TestProbe<DeviceManager.DeviceRegistered> probe = testKit.createTestProbe(DeviceManager.DeviceRegistered.class);
        ActorRef<DeviceGroup.Command> groupActor = testKit.spawn(DeviceGroup.create("group"));

        groupActor.tell(new DeviceManager.RequestTrackDevice("group", "device", probe.getRef()));
        DeviceManager.DeviceRegistered registered1 = probe.receiveMessage();

        // registering same again should be idempotent
        groupActor.tell(new DeviceManager.RequestTrackDevice("group", "device", probe.getRef()));
        DeviceManager.DeviceRegistered registered2 = probe.receiveMessage();
        assertEquals(registered1.device, registered2.device);
    }

    @Test
    public void testListActiveDevices() {
        TestProbe<DeviceManager.DeviceRegistered> registeredProbe = testKit.createTestProbe(DeviceManager.DeviceRegistered.class);
        ActorRef<DeviceGroup.Command> groupActor = testKit.spawn(DeviceGroup.create("group"));

        groupActor.tell(new DeviceManager.RequestTrackDevice("group", "device1", registeredProbe.getRef()));
        registeredProbe.receiveMessage();

        groupActor.tell(new DeviceManager.RequestTrackDevice("group", "device2", registeredProbe.getRef()));
        registeredProbe.receiveMessage();

        TestProbe<DeviceManager.ReplyDeviceList> deviceListProbe = testKit.createTestProbe(DeviceManager.ReplyDeviceList.class);

        groupActor.tell(new DeviceManager.RequestDeviceList(0L, "group", deviceListProbe.getRef()));
        DeviceManager.ReplyDeviceList reply = deviceListProbe.receiveMessage();
        assertEquals(0L, reply.requestId);
        assertEquals(Stream.of("device1", "device2").collect(Collectors.toSet()), reply.ids);
    }

    @Test
    public void testListActiveDevicesAfterOneShutsDown() {
        TestProbe<DeviceManager.DeviceRegistered> registeredProbe = testKit.createTestProbe(DeviceManager.DeviceRegistered.class);
        ActorRef<DeviceGroup.Command> groupActor = testKit.spawn(DeviceGroup.create("group"));

        groupActor.tell(new DeviceManager.RequestTrackDevice("group", "device1", registeredProbe.getRef()));
        DeviceManager.DeviceRegistered registered1 = registeredProbe.receiveMessage();

        groupActor.tell(new DeviceManager.RequestTrackDevice("group", "device2", registeredProbe.getRef()));
        DeviceManager.DeviceRegistered registered2 = registeredProbe.receiveMessage();

        ActorRef<Device.Command> toShutDown = registered1.device;

        TestProbe<DeviceManager.ReplyDeviceList> deviceListProbe = testKit.createTestProbe(DeviceManager.ReplyDeviceList.class);

        groupActor.tell(new DeviceManager.RequestDeviceList(0L, "group", deviceListProbe.getRef()));
        DeviceManager.ReplyDeviceList reply = deviceListProbe.receiveMessage();
        assertEquals(0L, reply.requestId);
        assertEquals(Stream.of("device1", "device2").collect(Collectors.toSet()), reply.ids);

        toShutDown.tell(Device.Passivate.INSTANCE);
        registeredProbe.expectTerminated(toShutDown, registeredProbe.getRemainingOrDefault());

        // using awaitAssert to retry because it might take longer for the groupActor
        // to see the Terminated, that order is undefined
        registeredProbe.awaitAssert(
                () -> {
                    groupActor.tell(new DeviceManager.RequestDeviceList(1L, "group", deviceListProbe.getRef()));
                    DeviceManager.ReplyDeviceList r = deviceListProbe.receiveMessage();
                    assertEquals(1L, r.requestId);
                    assertEquals(Stream.of("device2").collect(Collectors.toSet()), r.ids);
                    return null;
                });
    }
}
