package com.hm.achievement.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import com.hm.achievement.AdvancedAchievements;
import com.hm.achievement.category.NormalAchievements;
import com.hm.mcshared.event.PlayerChangeAnimalOwnershipEvent;

/**
 * Listener class to deal with PetMasterGive and PetMasterReceive achievements.
 * 
 * @author Pyves
 *
 */
public class AchievePetMasterGiveReceiveListener extends AbstractListener implements Listener {

	public AchievePetMasterGiveReceiveListener(AdvancedAchievements plugin) {
		super(plugin);
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onChangeOwnership(PlayerChangeAnimalOwnershipEvent event) {
		Player giverPlayer = (Player) event.getOldOwner();
		NormalAchievements categoryGive = NormalAchievements.PETMASTERGIVE;
		if (!shouldEventBeTakenIntoAccount(giverPlayer, categoryGive)) {
			return;
		}

		if (!plugin.getDisabledCategorySet().contains(categoryGive.toString())) {
			updateStatisticAndAwardAchievementsIfAvailable(giverPlayer, categoryGive, 1);
		}

		Player receiverPlayer = (Player) event.getNewOwner();
		NormalAchievements categoryReceive = NormalAchievements.PETMASTERRECEIVE;

		if (!shouldEventBeTakenIntoAccount(receiverPlayer, categoryReceive)) {
			return;
		}

		if (!plugin.getDisabledCategorySet().contains(categoryReceive.toString())) {
			updateStatisticAndAwardAchievementsIfAvailable(receiverPlayer, categoryReceive, 1);
		}
	}
}
