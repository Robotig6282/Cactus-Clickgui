package xyz.robotig.cactusclickgui.module.impl;

import com.dwarslooper.cactus.client.addon.v2.ICactusAddon;
import com.dwarslooper.cactus.client.addon.v2.RegistryBus;
import com.dwarslooper.cactus.client.feature.module.Module;

public class CactusClickguiAddon implements ICactusAddon {
    @Override
    public void onInitialize(RegistryBus registryBus) {
        registryBus.register(Module.class, context -> new ClickGuiModule());
    }
}
