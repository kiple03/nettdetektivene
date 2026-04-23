package org.ntnu.backend.config;

import java.util.List;
import org.ntnu.backend.avatar.model.AvatarItem;
import org.ntnu.backend.avatar.repository.AvatarItemRepository;
import org.springframework.stereotype.Component;
import org.springframework.boot.CommandLineRunner;
/**
 * Config class. It seeds the database with starter items for 
 * avatars. 
 */
@Component
public class AvatarDataSeeder implements CommandLineRunner{
    private final AvatarItemRepository itemRepository;

    public AvatarDataSeeder(AvatarItemRepository itemRepository){
        this.itemRepository = itemRepository;
    } 

    /**
     * Method that runs automatically with spring boot upon startup. 
     */
    @Override
    public void run(String... args) throws Exception{
        if(itemRepository.count() == 0){
            System.out.println("Empty avatar table. Seeding with items...");

            AvatarItem detective_hat = AvatarItem.builder().name("detective_hat").category("head").build();
            AvatarItem detective_suit = AvatarItem.builder().name("detective_suit").category("body").build();
            AvatarItem face_1 = AvatarItem.builder().name("face_1").category("face").build();
            AvatarItem face_2 = AvatarItem.builder().name("face_2").category("face").build();
            AvatarItem face_3 = AvatarItem.builder().name("face_3").category("face").build();
            AvatarItem magnifying_glass = AvatarItem.builder().name("magnifying_glass").category("accessories").build();
            AvatarItem police_hat = AvatarItem.builder().name("police_hat").category("head").build();
            AvatarItem police_uniform = AvatarItem.builder().name("police_uniform").category("body").build();
            AvatarItem sheriff_hat = AvatarItem.builder().name("sheriff_hat").category("head").build();
            AvatarItem sheriff_star = AvatarItem.builder().name("sheriff_star").category("accessories").build();
            AvatarItem sheriff_suit = AvatarItem.builder().name("sheriff_suit").category("body").build();
            AvatarItem stop_sign = AvatarItem.builder().name("stop_sign").category("accessories").build();
            AvatarItem skin_light = AvatarItem.builder().name("tone_light").category("skin_tone").build();
            AvatarItem skin_medium = AvatarItem.builder().name("tone_medium").category("skin_tone").build();
            AvatarItem skin_dark = AvatarItem.builder().name("tone_dark").category("skin_tone").build();
            AvatarItem skin_wacky = AvatarItem.builder().name("tone_wacky").category("skin_tone").build();

            itemRepository.saveAll(List.of(magnifying_glass, face_1, face_2, face_3, detective_hat, detective_suit, police_hat, police_uniform, sheriff_hat, sheriff_star, sheriff_suit,stop_sign, skin_light, skin_medium, skin_dark, skin_wacky));
        }
    }
}
