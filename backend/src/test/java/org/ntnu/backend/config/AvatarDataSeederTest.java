package org.ntnu.backend.config;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ntnu.backend.avatar.model.AvatarItem;
import org.ntnu.backend.avatar.repository.AvatarItemRepository;

class AvatarDataSeederTest {

    private AvatarItemRepository avatarItemRepository;
    private AvatarDataSeeder avatarDataSeeder;

    @BeforeEach
    void setUp() {
        avatarItemRepository = mock(AvatarItemRepository.class);
        avatarDataSeeder = new AvatarDataSeeder(avatarItemRepository);
    }

    @Test
    void run_savesAllItemsWhenDatabaseIsEmpty() throws Exception {
        when(avatarItemRepository.findAll()).thenReturn(Collections.emptyList());

        avatarDataSeeder.run();

        verify(avatarItemRepository).saveAll(anyList());
    }

    @Test
    void run_doesNotSaveWhenAllItemsAlreadyExist() throws Exception {
        List<AvatarItem> existing = List.of(
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
                AvatarItem.builder().name("face_girl").category("face").build()
        );
        when(avatarItemRepository.findAll()).thenReturn(existing);

        avatarDataSeeder.run();

        verify(avatarItemRepository, never()).saveAll(anyList());
    }

    @Test
    void run_savesOnlyMissingItems() throws Exception {
        List<AvatarItem> existing = List.of(
                AvatarItem.builder().name("detective_hat").category("head").build()
        );
        when(avatarItemRepository.findAll()).thenReturn(existing);

        avatarDataSeeder.run();

        verify(avatarItemRepository).saveAll(argThat(items -> {
            List<?> list = (List<?>) items;
            // 19 total - 1 existing = 18 new items
            return list.size() == 18;
        }));
    }

    @Test
    void run_savesItemsWithCorrectCategories() throws Exception {
        when(avatarItemRepository.findAll()).thenReturn(Collections.emptyList());

        avatarDataSeeder.run();

        verify(avatarItemRepository).saveAll(argThat(items -> {
            List<AvatarItem> list = (List<AvatarItem>) items;
            return list.stream().anyMatch(i -> "head".equals(i.getCategory()))
                    && list.stream().anyMatch(i -> "body".equals(i.getCategory()))
                    && list.stream().anyMatch(i -> "face".equals(i.getCategory()))
                    && list.stream().anyMatch(i -> "skin_tone".equals(i.getCategory()))
                    && list.stream().anyMatch(i -> "hair".equals(i.getCategory()))
                    && list.stream().anyMatch(i -> "accessories".equals(i.getCategory()));
        }));
    }
}