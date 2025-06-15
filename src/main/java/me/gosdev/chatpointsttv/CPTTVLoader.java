package me.gosdev.chatpointsttv;

import java.io.File;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

import revxrsal.zapper.Dependency;
import revxrsal.zapper.DependencyManager;
import revxrsal.zapper.ZapperJavaPlugin;
import revxrsal.zapper.classloader.URLClassLoaderWrapper;
import revxrsal.zapper.relocation.Relocation;
import revxrsal.zapper.repository.Repository;
import revxrsal.zapper.transitive.TransitiveResolver;
import revxrsal.zapper.util.ClassLoaderReader;

public abstract class CPTTVLoader extends JavaPlugin {
    static {
        String relocationBase = "me.gosdev.chatpointsttv.libraries.";
        ArrayList<Dependency> deps = new ArrayList<Dependency>() {
            {
                add(new Dependency("com.github.twitch4j", "twitch4j", "1.25.0"));
                add(new Dependency("com.github.philippheuer.events4j", "events4j-handler-simple", "0.12.2"));
                add(new Dependency("org.json", "json", "20250517"));
            }
        };

        List<Repository> repos = new ArrayList<Repository>(){
            {
                add(Repository.mavenCentral());
                add(Repository.jitpack());
            }
        };

        File libraries = new File(
                ClassLoaderReader.getDataFolder(CPTTVLoader.class),
                "libraries" // libraries folder
        );
        if (!libraries.exists()) {
            PluginDescriptionFile pdf = ClassLoaderReader.getDescription(ZapperJavaPlugin.class);
            Bukkit.getLogger().info("[" + pdf.getName() + "] It appears you're running " + pdf.getName() + " for the first time.");
            Bukkit.getLogger().info("[" + pdf.getName() + "] Please give me a few seconds to install dependencies. This is a one-time process.");
        }
        DependencyManager dependencyManager = new DependencyManager(
                libraries,
                URLClassLoaderWrapper.wrap((URLClassLoader) CPTTVLoader.class.getClassLoader())
        );
        TransitiveResolver tr = TransitiveResolver.builder()
            .recursively(true)
            .repositories(repos)
            .build();


        // add your repositories
        for (Repository r : repos) {
            dependencyManager.repository(r);
        }

        // add your dependencies
        dependencyManager.dependency(deps);
        for (Dependency d : deps) {
            dependencyManager.dependency(tr.resolve(d));
        }

        // IMPORTANT NOTE: Beware that this path may get relocated/changed
        // by your build tool!!! Escape it using runtime tricks if necessary
        dependencyManager.relocate(new Relocation(
                "com{}github{}twitch4j{}twitch4j".replace("{}", "."),
                relocationBase + "twitch4j"
        ));
        dependencyManager.relocate(new Relocation(
                "com{}github{}philippheuer.events4j".replace("{}", "."),
                relocationBase + "events4j"
        ));
        dependencyManager.relocate(new Relocation(
                "org{}json{}json".replace("{}", "."),
                relocationBase + "json"
        ));

        dependencyManager.load();
    }
}
