package com.hm.achievement.listener;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

import com.hm.achievement.AdvancedAchievements;
import com.hm.achievement.category.MultipleAchievements;
import com.hm.achievement.category.NormalAchievements;
import com.hm.achievement.utils.AchievementCommentedYamlConfiguration;
import com.hm.achievement.utils.PlayerAdvancedAchievementEvent.PlayerAdvancedAchievementEventBuilder;
import com.hm.mcshared.particle.ReflectionUtils.PackageType;

/**
 * Abstract class in charge of factoring out common functionality for the listener classes.
 * 
 * @author Pyves
 */
public abstract class AbstractListener {

	protected final int version;
	protected final AdvancedAchievements plugin;

	protected AbstractListener(AdvancedAchievements plugin) {
		this.plugin = plugin;
		// Simple parsing of game version. Might need to be updated in the future depending on how the Minecraft
		// versions change in the future.
		version = Integer.parseInt(PackageType.getServerVersion().split("_")[1]);
	}

	/**
	 * Determines whether the listened event should be taken into account.
	 * 
	 * @param player
	 * @param category
	 * @return
	 */
	protected boolean shouldEventBeTakenIntoAccount(Player player, NormalAchievements category) {
		boolean isNPC = player.hasMetadata("NPC");
		boolean permission = player.hasPermission(category.toPermName());
		boolean restrictedCreative = plugin.isRestrictCreative() && player.getGameMode() == GameMode.CREATIVE;
		boolean restrictedSpectator = plugin.isRestrictSpectator() && player.getGameMode() == GameMode.SPECTATOR;
		boolean excludedWorld = plugin.isInExludedWorld(player);

		return !isNPC && permission && !restrictedCreative && !restrictedSpectator && !excludedWorld;
	}

	/**
	 * Determines whether the listened event should be taken into account. Ignore permission check.
	 * 
	 * @param player
	 * @param category
	 * @return
	 */
	protected boolean shouldEventBeTakenIntoAccountNoPermission(Player player) {
		boolean isNPC = player.hasMetadata("NPC");
		boolean restrictedCreative = plugin.isRestrictCreative() && player.getGameMode() == GameMode.CREATIVE;
		boolean restrictedSpectator = plugin.isRestrictSpectator() && player.getGameMode() == GameMode.SPECTATOR;
		boolean excludedWorld = plugin.isInExludedWorld(player);

		return !isNPC && !restrictedCreative && !restrictedSpectator && !excludedWorld;
	}

	/**
	 * Updates the statistic in the database for a NormalAchievement and awards an achievement if an available one is
	 * found.
	 * 
	 * @param player
	 * @param category
	 * @param incrementValue
	 */
	protected void updateStatisticAndAwardAchievementsIfAvailable(Player player, NormalAchievements category,
			int incrementValue) {
		long amount = plugin.getCacheManager().getAndIncrementStatisticAmount(category, player.getUniqueId(),
				incrementValue);

		if (incrementValue > 1) {
			// Every value must be checked to see whether it corresponds to an achievement's threshold.
			for (long threshold = amount - incrementValue + 1; threshold <= amount; ++threshold) {
				String configAchievement = category + "." + threshold;
				awardAchievementIfAvailable(player, configAchievement);
			}
		} else {
			String configAchievement = category + "." + amount;
			awardAchievementIfAvailable(player, configAchievement);
		}
	}

	/**
	 * Updates the statistic in the database for a MultipleAchievement and awards an achievement if an available one is
	 * found.
	 * 
	 * @param player
	 * @param category
	 * @param subcategory
	 * @param incrementValue
	 */
	protected void updateStatisticAndAwardAchievementsIfAvailable(Player player, MultipleAchievements category,
			String subcategory, int incrementValue) {
		long amount = plugin.getCacheManager().getAndIncrementStatisticAmount(category, subcategory,
				player.getUniqueId(), incrementValue);

		if (incrementValue > 1) {
			// Every value must be checked to see whether it corresponds to an achievement's threshold.
			for (long threshold = amount - incrementValue + 1; threshold <= amount; ++threshold) {
				String configAchievement = category + "." + subcategory + '.' + threshold;
				awardAchievementIfAvailable(player, configAchievement);
			}
		} else {
			String configAchievement = category + "." + subcategory + '.' + amount;
			awardAchievementIfAvailable(player, configAchievement);
		}
	}

	/**
	 * Awards an achievement if the corresponding threshold was found in the configuration file.
	 * 
	 * @param player
	 * @param configAchievement
	 */
	protected void awardAchievementIfAvailable(Player player, String configAchievement) {
		AchievementCommentedYamlConfiguration pluginConfig = plugin.getPluginConfig();
		if (pluginConfig.getString(configAchievement + ".Message", null) != null) {
			// Fire achievement event.
			PlayerAdvancedAchievementEventBuilder playerAdvancedAchievementEventBuilder = new PlayerAdvancedAchievementEventBuilder()
					.player(player).name(plugin.getPluginConfig().getString(configAchievement + ".Name"))
					.displayName(plugin.getPluginConfig().getString(configAchievement + ".DisplayName"))
					.message(plugin.getPluginConfig().getString(configAchievement + ".Message"))
					.commandRewards(plugin.getRewardParser().getCommandRewards(configAchievement, player))
					.itemReward(plugin.getRewardParser().getItemReward(configAchievement))
					.moneyReward(plugin.getRewardParser().getMoneyAmount(configAchievement));

			Bukkit.getServer().getPluginManager().callEvent(playerAdvancedAchievementEventBuilder.build());
		}
	}
}
