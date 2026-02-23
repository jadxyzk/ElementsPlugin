package com.elements.managers;

import com.elements.ElementsPlugin;
import com.elements.utils.Element;
import com.elements.utils.PlayerData;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;

public class AbilityManager {

    private final ElementsPlugin plugin;
    // cooldown map: UUID -> Map<"element+abilityNum", System.currentTimeMillis of last use>
    private final Map<UUID, Map<String, Long>> cooldowns = new HashMap<>();

    // Cooldown durations in milliseconds
    private final long COOLDOWN_1;
    private final long COOLDOWN_2;
    private final long COOLDOWN_3;

    public AbilityManager(ElementsPlugin plugin) {
        this.plugin = plugin;
        COOLDOWN_1 = plugin.getConfig().getLong("cooldowns.ability1", 30) * 1000L;
        COOLDOWN_2 = plugin.getConfig().getLong("cooldowns.ability2", 60) * 1000L;
        COOLDOWN_3 = plugin.getConfig().getLong("cooldowns.ability3", 90) * 1000L;
    }

    // ─── Entry Point ──────────────────────────────────────────────────────────

    public void useAbility(Player player, Element element, int abilityNum) {
        PlayerData data = plugin.getElementManager().getPlayerData(player);
        if (data == null) return;

        // Check it's their element
        if (data.getElement() != element) {
            player.sendMessage(format(plugin.getConfig().getString("messages.wrong-element", "&cThat is not your element!")));
            return;
        }

        // Check level (ability 1 = level 1+, ability 2 = level 2+, ability 3 = level 3)
        if (abilityNum > data.getLevel()) {
            player.sendMessage(format(plugin.getConfig().getString("messages.ability-locked", "&cYou need level %level% for that!")
                    .replace("%level%", String.valueOf(abilityNum))));
            return;
        }

        // Check cooldown
        String key = element.name() + abilityNum;
        long cooldownMs = getCooldownMs(abilityNum);
        long remaining = getRemainingCooldown(player.getUniqueId(), key);
        if (remaining > 0) {
            player.sendMessage(format(plugin.getConfig().getString("messages.on-cooldown", "&cOn cooldown! %time%s remaining.")
                    .replace("%time%", String.valueOf((int) Math.ceil(remaining / 1000.0)))));
            return;
        }

        // Fire the ability
        boolean success = fireAbility(player, element, abilityNum);
        if (success) {
            setCooldown(player.getUniqueId(), key, cooldownMs);
            plugin.getHudManager().updateHUD(player);
        }
    }

    private boolean fireAbility(Player player, Element element, int abilityNum) {
        switch (element) {
            case FIRE: return fireAbility(player, abilityNum);
            case WATER: return waterAbility(player, abilityNum);
            case EARTH: return earthAbility(player, abilityNum);
            case AIR: return airAbility(player, abilityNum);
        }
        return false;
    }

    // ─── FIRE Abilities ───────────────────────────────────────────────────────

    private boolean fireAbility(Player player, int num) {
        switch (num) {
            case 1: return fireIgnite(player);
            case 2: return fireball(player);
            case 3: return fireRing(player);
        }
        return false;
    }

    // Fire 1: Ignite nearby enemies (5 block radius)
    private boolean fireIgnite(Player player) {
        Location loc = player.getLocation();
        boolean hit = false;
        for (Entity e : player.getNearbyEntities(5, 5, 5)) {
            if (e instanceof LivingEntity && e != player) {
                ((LivingEntity) e).setFireTicks(60); // 3 seconds
                if (e instanceof Player) {
                    ((Player) e).sendMessage("§c🔥 You are being burned by " + player.getName() + "'s fire!");
                }
                hit = true;
            }
        }
        player.getWorld().spawnParticle(Particle.FLAME, loc, 40, 2, 1, 2, 0.05);
        player.playSound(loc, Sound.ENTITY_BLAZE_SHOOT, 1f, 1f);
        player.sendMessage("§c🔥 Ignite!");
        return true;
    }

    // Fire 2: Launch a fireball in the direction you're looking
    private boolean fireball(Player player) {
        Fireball fb = player.launchProjectile(Fireball.class);
        fb.setDirection(player.getLocation().getDirection().multiply(1.5));
        fb.setShooter(player);
        fb.setIsIncendiary(true);
        fb.setYield(2.0f);
        player.playSound(player.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 1f, 0.8f);
        player.sendMessage("§c🔥 Fireball launched!");
        return true;
    }

    // Fire 3: Ring of fire explosion around the player
    private boolean fireRing(Player player) {
        Location center = player.getLocation();
        double radius = 6;
        int points = 24;

        // Deal damage to nearby entities
        for (Entity e : player.getNearbyEntities(radius, radius, radius)) {
            if (e instanceof LivingEntity && e != player) {
                LivingEntity le = (LivingEntity) e;
                le.damage(8.0, player);
                le.setFireTicks(100);
                le.setVelocity(le.getLocation().toVector().subtract(center.toVector()).normalize().multiply(1.5).setY(0.5));
            }
        }

        // Spawn fire ring particles
        for (int i = 0; i < points; i++) {
            double angle = (2 * Math.PI / points) * i;
            double x = center.getX() + radius * Math.cos(angle);
            double z = center.getZ() + radius * Math.sin(angle);
            Location particleLoc = new Location(center.getWorld(), x, center.getY(), z);
            center.getWorld().spawnParticle(Particle.FLAME, particleLoc, 10, 0.2, 0.5, 0.2, 0.02);
            center.getWorld().spawnParticle(Particle.LAVA, particleLoc, 3, 0.1, 0.1, 0.1, 0);
        }

        center.getWorld().playSound(center, Sound.ENTITY_GENERIC_EXPLODE, 1f, 0.8f);
        player.sendMessage("§c🔥 Ring of Fire!");
        return true;
    }

    // ─── WATER Abilities ──────────────────────────────────────────────────────

    private boolean waterAbility(Player player, int num) {
        switch (num) {
            case 1: return waterSlow(player);
            case 2: return waterBurst(player);
            case 3: return waterHeal(player);
        }
        return false;
    }

    // Water 1: Slow nearby enemies
    private boolean waterSlow(Player player) {
        for (Entity e : player.getNearbyEntities(6, 6, 6)) {
            if (e instanceof LivingEntity && e != player) {
                ((LivingEntity) e).addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 100, 2));
                if (e instanceof Player) ((Player) e).sendMessage("§b💧 You've been slowed by " + player.getName() + "!");
            }
        }
        player.getWorld().spawnParticle(Particle.WATER_SPLASH, player.getLocation(), 80, 2, 1, 2, 0.1);
        player.playSound(player.getLocation(), Sound.WEATHER_RAIN, 1f, 1.5f);
        player.sendMessage("§b💧 Tidal Slow!");
        return true;
    }

    // Water 2: Knock enemies back
    private boolean waterBurst(Player player) {
        Location center = player.getLocation();
        for (Entity e : player.getNearbyEntities(5, 5, 5)) {
            if (e instanceof LivingEntity && e != player) {
                Vector dir = e.getLocation().toVector().subtract(center.toVector()).normalize();
                e.setVelocity(dir.multiply(2.5).setY(0.8));
                if (e instanceof LivingEntity) ((LivingEntity) e).damage(4.0, player);
                if (e instanceof Player) ((Player) e).sendMessage("§b💧 You were hit by " + player.getName() + "'s water burst!");
            }
        }
        player.getWorld().spawnParticle(Particle.WATER_SPLASH, center, 150, 3, 1, 3, 0.2);
        player.playSound(center, Sound.ENTITY_GENERIC_SPLASH, 1f, 0.5f);
        player.sendMessage("§b💧 Water Burst!");
        return true;
    }

    // Water 3: Heal yourself
    private boolean waterHeal(Player player) {
        double maxHealth = player.getMaxHealth();
        double currentHealth = player.getHealth();
        double healAmount = maxHealth * 0.5; // heal 50% max HP
        player.setHealth(Math.min(maxHealth, currentHealth + healAmount));
        player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 60, 1));
        player.getWorld().spawnParticle(Particle.HEART, player.getLocation().add(0, 1, 0), 20, 0.5, 0.5, 0.5, 0.1);
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.7f, 1.5f);
        player.sendMessage("§b💧 Tidal Healing! Restored 50% health.");
        return true;
    }

    // ─── EARTH Abilities ──────────────────────────────────────────────────────

    private boolean earthAbility(Player player, int num) {
        switch (num) {
            case 1: return earthLaunch(player);
            case 2: return earthWall(player);
            case 3: return earthQuake(player);
        }
        return false;
    }

    // Earth 1: Knock nearby enemies upward
    private boolean earthLaunch(Player player) {
        for (Entity e : player.getNearbyEntities(4, 4, 4)) {
            if (e instanceof LivingEntity && e != player) {
                e.setVelocity(new Vector(0, 2.5, 0));
                if (e instanceof Player) ((Player) e).sendMessage("§a🌍 You were launched by " + player.getName() + "!");
            }
        }
        player.getWorld().spawnParticle(Particle.BLOCK, player.getLocation(), 60,
                1, 0.5, 1, 0.1, Material.DIRT.createBlockData());
        player.playSound(player.getLocation(), Sound.BLOCK_GRAVEL_BREAK, 1f, 0.5f);
        player.sendMessage("§a🌍 Earth Launch!");
        return true;
    }

    // Earth 2: Summon a wall of stone blocks in front of the player
    private boolean earthWall(Player player) {
        Location loc = player.getLocation();
        org.bukkit.block.BlockFace face = getCardinalFace(loc.getYaw());
        List<Block> placedBlocks = new ArrayList<>();

        for (int height = 0; height < 3; height++) {
            for (int width = -1; width <= 1; width++) {
                Block b = loc.getBlock().getRelative(face, 2);
                // Offset horizontally
                Location bLoc = b.getLocation().add(0, height, 0);
                // Side offset
                if (width != 0) {
                    BlockFace sideface = (face == BlockFace.NORTH || face == BlockFace.SOUTH) ? BlockFace.EAST : BlockFace.NORTH;
                    bLoc = bLoc.getBlock().getRelative(sideface, width).getLocation();
                }
                Block wall = bLoc.getBlock();
                if (wall.getType() == Material.AIR) {
                    wall.setType(Material.STONE);
                    placedBlocks.add(wall);
                }
            }
        }

        player.playSound(loc, Sound.BLOCK_STONE_PLACE, 1f, 0.8f);
        player.sendMessage("§a🌍 Earth Wall summoned!");

        // Remove wall after 10 seconds
        new BukkitRunnable() {
            public void run() {
                for (Block b : placedBlocks) {
                    if (b.getType() == Material.STONE) b.setType(Material.AIR);
                }
            }
        }.runTaskLater(plugin, 200L);
        return true;
    }

    // Earth 3: Earthquake — AoE damage and slowness
    private boolean earthQuake(Player player) {
        Location center = player.getLocation();
        double radius = 8;

        for (Entity e : player.getNearbyEntities(radius, 4, radius)) {
            if (e instanceof LivingEntity && e != player) {
                LivingEntity le = (LivingEntity) e;
                le.damage(10.0, player);
                le.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 80, 3));
                le.addPotionEffect(new PotionEffect(PotionEffectType.MINING_FATIGUE, 80, 2));
                // Shake them
                le.setVelocity(new Vector(
                        (Math.random() - 0.5) * 1.5,
                        0.5,
                        (Math.random() - 0.5) * 1.5
                ));
                if (e instanceof Player) ((Player) e).sendMessage("§a🌍 You were caught in " + player.getName() + "'s earthquake!");
            }
        }

        // Particle effect spreading outward
        new BukkitRunnable() {
            int tick = 0;
            public void run() {
                if (tick >= 10) { cancel(); return; }
                double r = tick * 0.9;
                for (int i = 0; i < 20; i++) {
                    double angle = Math.random() * 2 * Math.PI;
                    double x = center.getX() + r * Math.cos(angle);
                    double z = center.getZ() + r * Math.sin(angle);
                    center.getWorld().spawnParticle(Particle.BLOCK, new Location(center.getWorld(), x, center.getY(), z),
                            5, 0.3, 0.3, 0.3, 0.05, Material.DIRT.createBlockData());
                }
                center.getWorld().playSound(center, Sound.BLOCK_GRAVEL_BREAK, 0.5f, 0.3f);
                tick++;
            }
        }.runTaskTimer(plugin, 0L, 2L);

        player.sendMessage("§a🌍 Earthquake!");
        return true;
    }

    // ─── AIR Abilities ────────────────────────────────────────────────────────

    private boolean airAbility(Player player, int num) {
        switch (num) {
            case 1: return airDash(player);
            case 2: return airLaunchEnemies(player);
            case 3: return airTornado(player);
        }
        return false;
    }

    // Air 1: Dash forward
    private boolean airDash(Player player) {
        Vector dir = player.getLocation().getDirection().normalize().multiply(2.5);
        dir.setY(0.4);
        player.setVelocity(dir);
        player.getWorld().spawnParticle(Particle.CLOUD, player.getLocation(), 30, 0.3, 0.3, 0.3, 0.05);
        player.playSound(player.getLocation(), Sound.ENTITY_PHANTOM_FLAP, 1f, 1.5f);
        player.sendMessage("§f🌬️ Air Dash!");
        return true;
    }

    // Air 2: Launch enemies into the air
    private boolean airLaunchEnemies(Player player) {
        for (Entity e : player.getNearbyEntities(6, 6, 6)) {
            if (e instanceof LivingEntity && e != player) {
                Vector dir = e.getLocation().toVector().subtract(player.getLocation().toVector()).normalize();
                dir.setY(3.0);
                e.setVelocity(dir);
                if (e instanceof Player) ((Player) e).sendMessage("§f🌬️ You were launched into the air by " + player.getName() + "!");
            }
        }
        player.getWorld().spawnParticle(Particle.CLOUD, player.getLocation(), 80, 3, 1, 3, 0.15);
        player.playSound(player.getLocation(), Sound.ENTITY_PHANTOM_FLAP, 1f, 0.5f);
        player.sendMessage("§f🌬️ Cyclone Launch!");
        return true;
    }

    // Air 3: Tornado — pulls enemies toward center
    private boolean airTornado(Player player) {
        Location center = player.getLocation();
        player.sendMessage("§f🌬️ Tornado!");

        new BukkitRunnable() {
            int tick = 0;
            public void run() {
                if (tick >= 40) { cancel(); return; } // 2 seconds

                // Pull nearby enemies in
                for (Entity e : player.getNearbyEntities(8, 8, 8)) {
                    if (e instanceof LivingEntity && e != player) {
                        Vector pull = center.toVector().subtract(e.getLocation().toVector()).normalize().multiply(0.6);
                        pull.setY(0.3);
                        e.setVelocity(pull);
                        if (tick % 10 == 0) {
                            ((LivingEntity) e).damage(3.0, player);
                            if (e instanceof Player) ((Player) e).sendMessage("§f🌬️ You're caught in " + player.getName() + "'s tornado!");
                        }
                    }
                }

                // Spiral particles
                double angle = tick * 0.5;
                for (int i = 0; i < 3; i++) {
                    double a = angle + (i * 2 * Math.PI / 3);
                    double radius = 3;
                    double x = center.getX() + radius * Math.cos(a);
                    double z = center.getZ() + radius * Math.sin(a);
                    double y = center.getY() + (tick * 0.1);
                    center.getWorld().spawnParticle(Particle.CLOUD, new Location(center.getWorld(), x, y, z), 3, 0.1, 0.1, 0.1, 0.02);
                }

                tick++;
            }
        }.runTaskTimer(plugin, 0L, 1L);

        player.playSound(center, Sound.ENTITY_PHANTOM_FLAP, 1f, 0.3f);
        return true;
    }

    // ─── Cooldown Utilities ───────────────────────────────────────────────────

    private void setCooldown(UUID uuid, String key, long durationMs) {
        cooldowns.computeIfAbsent(uuid, k -> new HashMap<>()).put(key, System.currentTimeMillis() + durationMs);
    }

    public long getRemainingCooldown(UUID uuid, String key) {
        Map<String, Long> map = cooldowns.get(uuid);
        if (map == null) return 0;
        Long expiry = map.get(key);
        if (expiry == null) return 0;
        long remaining = expiry - System.currentTimeMillis();
        return Math.max(0, remaining);
    }

    public Map<String, Long> getCooldowns(UUID uuid) {
        return cooldowns.getOrDefault(uuid, new HashMap<>());
    }

    private long getCooldownMs(int abilityNum) {
        switch (abilityNum) {
            case 1: return COOLDOWN_1;
            case 2: return COOLDOWN_2;
            case 3: return COOLDOWN_3;
        }
        return COOLDOWN_1;
    }

    // ─── Helper: Get cardinal block face from yaw ─────────────────────────────

    private BlockFace getCardinalFace(float yaw) {
        yaw = yaw % 360;
        if (yaw < 0) yaw += 360;
        if (yaw < 45 || yaw >= 315) return BlockFace.SOUTH;
        if (yaw < 135) return BlockFace.WEST;
        if (yaw < 225) return BlockFace.NORTH;
        return BlockFace.EAST;
    }

    private String format(String msg) {
        return msg.replace("&", "§");
    }
}
