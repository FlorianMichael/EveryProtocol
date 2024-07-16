# Developer API
ViaFabricPlus provides various events and APIs for developers to use. This page explains how to use them.

## Include via Gradle/Maven
```groovy
repositories {
    mavenCentral()
    maven { 
        name = "ViaVersion"
        url = "https://repo.viaversion.com"
    }
    maven {
        name = "Lenni0451"
        url = "https://maven.lenni0451.net/everything"
    }
    maven {
        name = "OpenCollab Snapshots"
        url = "https://repo.opencollab.dev/maven-snapshots/"
    }
}

dependencies {
    modImplementation("de.florianmichael:ViaFabricPlus:x.x.x") // Get the latest version from releases
}
```

```xml
<repositories>
    <repository>
        <id>viaversion</id>
        <url>https://repo.viaversion.com</url>
    </repository>
    <repository>
        <id>lenni0451</id>
        <url>https://maven.lenni0451.net/everything</url>
    </repository>
    <repository>
        <id>opencollab-snapshots</id>
        <url>https://repo.opencollab.dev/maven-snapshots/</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>de.florianmichael</groupId>
        <artifactId>ViaFabricPlus</artifactId>
        <version>x.x.x</version> <!-- Get the latest version from releases -->
    </dependency>
</dependencies>
```

## Events
ViaFabricPlus events are using the [Fabric Event API](https://fabricmc.net/wiki/tutorial:events). You can register to them like this:
```java
ChangeProtocolVersionCallback.EVENT.register((oldVersion, newVersion) -> {
    System.out.println("Version changed to " + newVersion.getName());
});
```
### ViaFabricPlus has 8 events at the moment
| Callback class name                  | Description                                                                                                                                                                                                   |
|--------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| ChangeProtocolVersionCallback        | Called when the user changes the target version in the screen, or if you connect to a server for which a specific version has been selected, you disconnect, the event for the actual version is also called. |
| DisconnectCallback                   | Called when the user disconnects from a server                                                                                                                                                                |
| LoadCallback                         | Called at the earliest point ViaFabricPlus is injecting too                                                                                                                                                   |
| LoadClassicProtocolExtensionCallback | Called when the classic server sends the protocol extensions (only in **c0.30 CPE**)                                                                                                                          |
| PostGameLoadCallback                 | Called when Minecraft is finished with loading all its components                                                                                                                                             |
| PostViaVersionLoadCallback           | Called when ViaVersion is loaded and ready to use                                                                                                                                                             |
| RegisterSettingsCallback             | Called after the default setting groups are loaded and before the setting config is loaded                                                                                                                    |
| LoadSaveFilesCallback                | Called before and after the save files are loaded                                                                                                                                                             |

## Get and set the current protocol version
```java
final ProtocolVersion version = ProtocolTranslator.getTargetVersion();
if (version == ProtocolVersion.v1_8) {
    ProtocolTranslator.setTargetVersion(ProtocolVersion.v1_9);
}
```

## Get a Minecraft ClientConnection by channel
```java
final ClientConnection connection = channel.attr(ProtocolTranslator.CLIENT_CONNECTION_ATTRIBUTE_KEY).get();
```

## Interact with UserConnection objects
```java
// If ViaVersion is translating, this field will return the user connection of the client
final UserConnection userConnection = ProtocolTranslator.getPlayNetworkUserConnection();

// If you need a dummy user connection for testing, you can use this method
final UserConnection cursedDummy = ProtocolTranslator.createDummyUserConnection(ProtocolTranslator.NATIVE_VERSION, ProtocolVersion.v1_18_2);
// The cursedDummy field now contains all protocols from the native version to 1.18.2
```

## ViaVersion internals
### Add CustomPayload channels for versions below 1.13
In order to receive custom payloads with custom channels in versions below 1.13, you need to register them, that's what you do:
```java
Protocol1_13To1_12_2.MAPPINGS.getChannelMappings().put("FML|HS", "fml:hs");
```

### Check if an item exists in a specific version
```java
final VersionRange range = ItemRegistryDiff.ITEM_DIFF.get(Items.WRITABLE_BOOK); // If an item does not appear in the item map, it has always existed

// The Range class then contains all versions in which the item occurs. 
// https://github.com/ViaVersion/ViaLoader
if (ItemRegistryDiff.contains(Items.STONE, VersionRange.andOlder(ProtocolVersion.v1_8))) {
    // Do something
}
```
