package com.github.razorplay01.entity.custom.util;

import com.github.razorplay01.entity.custom.BaseCuadroEntity;
import com.github.razorplay01.entity.custom.PuertaJaulaEntity;
import com.github.razorplay01.entity.custom.PuertaMetalicaEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

public class PuzzleEntityChecker {

    private static final List<AnomalyChecker> CHECKERS = new ArrayList<>();

    public static void registerChecker(AnomalyChecker checker) {
        CHECKERS.add(checker);
    }

    public static boolean hasAnyAnomaly(Level level, AABB area) {
        for (AnomalyChecker checker : CHECKERS) {
            if (checker.check(level, area)) {
                return true;
            }
        }
        return false;
    }

    public interface AnomalyChecker {
        boolean check(Level level, AABB area);
    }

    public static void registerDefaultCheckers() {
        registerChecker((level, area) ->
                level.getEntitiesOfClass(BaseCuadroEntity.class, area).stream()
                        .anyMatch(cuadro -> cuadro.hasBeenMoved() && !cuadro.isPuzzleSolved())
        );

        registerChecker((level, area) ->
                level.getEntitiesOfClass(PuertaJaulaEntity.class, area).stream()
                        .anyMatch(PuertaJaulaEntity::isOpen)
        );

        registerChecker((level, area) ->
                level.getEntitiesOfClass(PuertaMetalicaEntity.class, area).stream()
                        .anyMatch(PuertaMetalicaEntity::isOpen)
        );
        //palanca
        //interruptor
        // posion del player dentro de una region en concreto

    }
}