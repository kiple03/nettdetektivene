package org.ntnu.backend.config;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.ntnu.backend.avatar.model.AvatarItem;
import org.ntnu.backend.avatar.repository.AvatarItemRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Seeds the database with the default set of avatar items on application startup.
 * Items are only inserted if they do not already exist (idempotent by name).
 */
@Component
@Order(1)
public class AvatarDataSeeder implements CommandLineRunner{
    private final AvatarItemRepository itemRepository;

    /**
     * @param itemRepository repository used to check existing items and persist new ones
     */
    public AvatarDataSeeder(AvatarItemRepository itemRepository){
        this.itemRepository = itemRepository;
    }

    /**
     * Inserts any missing avatar items into the database.
     *
     * @param args ignored command-line arguments
     * @throws Exception if persistence fails
     */
    @Override
    public void run(String... args) throws Exception{
        Set<String> existingNames = itemRepository.findAll().stream()
            .map(AvatarItem::getName)
            .collect(Collectors.toSet());

        List<AvatarItem> allItems = List.of(
            AvatarItem.builder().name("detective_hat").category("head").build(),
            AvatarItem.builder().name("detective_suit").category("body").build(),
            AvatarItem.builder().name("face_1").category("face").build(),
            AvatarItem.builder().name("face_2").category("face").build(),
            AvatarItem.builder().name("face_3").category("face").build(),
            AvatarItem.builder().name("magnifying_glass").category("accessories").build(),
            AvatarItem.builder().name("police_hat").category("head").build(),
            AvatarItem.builder().name("police_uniform").category("body").build(),
            AvatarItem.builder().name("sheriff_hat").category("head").build(),
            AvatarItem.builder().name("sheriff_star").category("accessories").build(),
            AvatarItem.builder().name("sheriff_suit").category("body").build(),
            AvatarItem.builder().name("stop_sign").category("accessories").build(),
            AvatarItem.builder().name("tone_light").category("skin_tone").build(),
            AvatarItem.builder().name("tone_medium").category("skin_tone").build(),
            AvatarItem.builder().name("tone_dark").category("skin_tone").build(),
            AvatarItem.builder().name("tone_wacky").category("skin_tone").build(),
            AvatarItem.builder().name("boy_hair").category("hair").build(),
            AvatarItem.builder().name("girl_hair").category("hair").build(),
            AvatarItem.builder().name("afro").category("hair").build(),
            AvatarItem.builder().name("girl_bob_hair").category("hair").build(),
            AvatarItem.builder().name("mohawk").category("hair").build(),
            AvatarItem.builder().name("face_girl").category("face").imageFile("face_girl.png").build(),
            AvatarItem.builder().name("face_girl_2").category("face").imageFile("face_girl_2.png").build(),
            AvatarItem.builder().name("face_girl_3").category("face").imageFile("girl_face_3.png").build(),
            AvatarItem.builder().name("VR_goggles").category("head").imageFile("boss-goggles.png").unlockCondition("BOSS_DEFEAT").build(),
            AvatarItem.builder().name("burning_hand").category("accessories").imageFile("burning-hand.gif").build(),
            AvatarItem.builder().name("armor").category("body").imageFile("armor.gif").build(),
            AvatarItem.builder().name("stars").category("head").imageFile("stars.gif").build()
        );

        List<AvatarItem> toSave = allItems.stream()
            .filter(item -> !existingNames.contains(item.getName()))
            .toList();

        if (!toSave.isEmpty()) {
            System.out.println("Seeding " + toSave.size() + " new avatar item(s)...");
            itemRepository.saveAll(toSave);
        }
    }
}
