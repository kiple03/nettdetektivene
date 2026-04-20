package org.ntnu.backend.avatar.config;

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

            AvatarItem cap_blue = AvatarItem.builder().name("cap_blue").category("head").build();
            AvatarItem glasses = AvatarItem.builder().name("glasses").category("head").build();
            AvatarItem necklace = AvatarItem.builder().name("necklace").category("head").build();
            AvatarItem shirt_red = AvatarItem.builder().name("shirt_red").category("body").build();

            itemRepository.saveAll(List.of(cap_blue, glasses, necklace, shirt_red));
        }
    }
}
