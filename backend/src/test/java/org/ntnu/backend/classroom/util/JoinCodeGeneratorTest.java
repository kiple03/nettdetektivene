package org.ntnu.backend.classroom.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ntnu.backend.classroom.repository.ClassroomRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import static org.mockito.Mockito.mock;

class JoinCodeGeneratorTest {

    private ClassroomRepository classroomRepository;
    private JoinCodeGenerator joinCodeGenerator;

    @BeforeEach
    void setUp() {
        classroomRepository = mock(ClassroomRepository.class);
        joinCodeGenerator = new JoinCodeGenerator(classroomRepository);
    }

    @Test
    void generateUniqueCode_whenGeneratedCodeDoesNotExist_shouldReturnSixCharacterCode() {
        when(classroomRepository.existsByJoinCode(anyString())).thenReturn(false);

        String result = joinCodeGenerator.generateUniqueCode();

        assertThat(result).hasSize(6);
        assertThat(result).matches("[ABCDEFGHJKLMNPQRSTUVWXYZ23456789]{6}");

        verify(classroomRepository).existsByJoinCode(result);
    }

    @Test
    void generateUniqueCode_shouldOnlyUseAllowedCharacters() {
        when(classroomRepository.existsByJoinCode(anyString())).thenReturn(false);

        String result = joinCodeGenerator.generateUniqueCode();

        assertThat(result).doesNotContain("I");
        assertThat(result).doesNotContain("O");
        assertThat(result).doesNotContain("0");
        assertThat(result).doesNotContain("1");
        assertThat(result).matches("[ABCDEFGHJKLMNPQRSTUVWXYZ23456789]{6}");
    }

    @Test
    void generateUniqueCode_whenFirstCodesCollide_shouldKeepTryingUntilUniqueCodeIsFound() {
        AtomicInteger calls = new AtomicInteger();

        when(classroomRepository.existsByJoinCode(anyString()))
                .thenAnswer(invocation -> calls.incrementAndGet() <= 3);

        String result = joinCodeGenerator.generateUniqueCode();

        assertThat(result).hasSize(6);
        assertThat(result).matches("[ABCDEFGHJKLMNPQRSTUVWXYZ23456789]{6}");
        assertThat(calls.get()).isEqualTo(4);

        verify(classroomRepository, times(4)).existsByJoinCode(anyString());
    }

    @Test
    void generateUniqueCode_whenAllAttemptsCollide_shouldThrowInternalServerError() {
        when(classroomRepository.existsByJoinCode(anyString())).thenReturn(true);

        assertThatThrownBy(() -> joinCodeGenerator.generateUniqueCode())
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> {
                    ResponseStatusException exception = (ResponseStatusException) error;
                    assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
                    assertThat(exception.getReason()).isEqualTo("Kunne ikke generere unik kode");
                });

        verify(classroomRepository, times(20)).existsByJoinCode(anyString());
    }

    @Test
    void generateUniqueCode_shouldAskRepositoryWithSixCharacterCodes() {
        when(classroomRepository.existsByJoinCode(anyString())).thenReturn(false);

        joinCodeGenerator.generateUniqueCode();

        verify(classroomRepository).existsByJoinCode(org.mockito.ArgumentMatchers.argThat(
                code -> code != null
                        && code.length() == 6
                        && code.matches("[ABCDEFGHJKLMNPQRSTUVWXYZ23456789]{6}")
        ));
    }
}