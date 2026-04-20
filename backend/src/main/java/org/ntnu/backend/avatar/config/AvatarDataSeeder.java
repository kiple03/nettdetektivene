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

            AvatarItem magnifying_glass = AvatarItem.builder().name("magnifying_glass").category("accessories").build();
            AvatarItem face_1 = AvatarItem.builder().name("face_1").category("face").build();
            AvatarItem detective_hat = AvatarItem.builder().name("detective_hat").category("head").build();
            AvatarItem detective_suit = AvatarItem.builder().name("detective_suit").category("body").build();

            itemRepository.saveAll(List.of(magnifying_glass, face_1, detective_hat, detective_suit));
        }
    }
}
