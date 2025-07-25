ELEPHANT_STRENGTH = 5
SUPER_MARIO_STRENGTH = 4
SMALL_STRENGTH = 3


def get_collision_response(hardness, blast_resistance, strength):
    hardness += EPSILON  # accounts for floating point imprecision

    if strength == SUPER_MARIO_STRENGTH:
        if hardness < 0.25:
            return THRU

        if hardness < 0.6:
            return BREAK

        if hardness < 1.55:
            return CRACK

        if hardness < 3.5:
            return BUMP

    elif strength == SMALL_STRENGTH:
        if hardness < 0.2:
            return THRU

        if hardness < 0.4:
            return BREAK

        if hardness < 0.6:
            return CRACK

        if hardness < 2.6:
            return BUMP

    return NO_SELL


# Editable code ends here!

EPSILON = 0.000001
NO_SELL, BUMP, CRACK, BREAK, THRU = range(5)
collisionResponseNames = ["NO-SELL", "BUMP", "CRACK", "BREAK", "GO-THRU"]

testsRun, testsPassed = 0, 0


def test_response(test_name, expected_result, got_result):
    global testsRun, testsPassed
    testsRun += 1
    if type(expected_result) is tuple:
        acceptable_results = ""
        for acceptable_result in expected_result:
            if len(acceptable_results) != 0:
                acceptable_results += ", "
            acceptable_results += collisionResponseNames[acceptable_result]

        if got_result in expected_result:
            print(f"Test {testsRun}, {test_name}, PASSED (Got {collisionResponseNames[got_result]}, which is OK)\n")
            testsPassed += 1
        else:
            print(
                f"* Test {testsRun}, {test_name}, FAILED\n\tGot {collisionResponseNames[got_result]}, expected one of ({acceptable_results})\n")
    else:
        if expected_result == got_result:
            print(f"Test {testsRun}, {test_name}, PASSED (Got expected {collisionResponseNames[got_result]})\n")
            testsPassed += 1
        else:
            print(
                f"* Test {testsRun}, {test_name}, FAILED\n\tGot {collisionResponseNames[got_result]}, expected {collisionResponseNames[expected_result]}\n")


if __name__ == "__main__":
    print("Testing the collision response function:\n")

    print("SUPER MARIO: ------------------------------\n")

    test_response("Super Pound on Moss Block", THRU, get_collision_response(0.1, 0.1, SUPER_MARIO_STRENGTH))

    # Threshold at which Super Mario can no longer go THRU blocks (between 0.1-0.3)

    test_response("Super Pound on Glass", BREAK, get_collision_response(0.3, 0.3, SUPER_MARIO_STRENGTH))

    test_response("Super Pound on Dirt", BREAK, get_collision_response(0.5, 0.5, SUPER_MARIO_STRENGTH))

    # The threshold at which Super Mario can no longer BREAK blocks (between 0.5-0.6)

    test_response("Super Pound on Grass Block", CRACK, get_collision_response(0.6, 0.6, SUPER_MARIO_STRENGTH))

    test_response("Super pound on Stone", CRACK, get_collision_response(1.5, 6, SUPER_MARIO_STRENGTH))

    # The threshold at which Super Mario can no longer CRACK blocks (between 1.5-2.0)

    test_response("Super Pound on Wooden Planks", BUMP, get_collision_response(2, 3, SUPER_MARIO_STRENGTH))

    test_response("Super Pound on Copper", BUMP, get_collision_response(3, 6, SUPER_MARIO_STRENGTH))

    # The threshold at which Super Mario can no longer BUMP blocks (between 2.0-5.0)

    test_response("Super Pound on Iron Block", NO_SELL, get_collision_response(5, 6, SUPER_MARIO_STRENGTH))


    print("SMALL MARIO (OR SUPER MARIO ON WALL): ------------------------------\n")

    test_response("Small Pound on Moss Block", THRU, get_collision_response(0.1, 0.1, 3))

    # The threshold at which Small Mario can no longer go THRU blocks (between 0.1-0.2)

    test_response("Small Pound on Leaves", BREAK, get_collision_response(0.2, 0.2, SMALL_STRENGTH))

    test_response("Small Pound on Glass", BREAK, get_collision_response(0.3, 0.3, SMALL_STRENGTH))

    # The threshold at which Small Mario can no longer BREAK blocks (between 0.3-0.4)

    test_response("Small Pound on Netherrack", CRACK, get_collision_response(0.4, 0.4, SMALL_STRENGTH))

    # The threshold at which Small Mario can no longer CRACK blocks (between 0.4-0.6)

    test_response("Small Pound on Grass Block", BUMP, get_collision_response(0.6, 0.6, SMALL_STRENGTH))

    test_response("Small Pound on Wooden Planks", BUMP, get_collision_response(2, 3, SMALL_STRENGTH))

    # The threshold at which Small Mario can no longer BUMP blocks (between 2.0-5.0)

    test_response("Super Pound on Iron Block", NO_SELL, get_collision_response(5, 6, SUPER_MARIO_STRENGTH))

    print("ELEPHANT MARIO?: ------------------------------\n")

    test_response("Elephant Pound on Netherrack", THRU, get_collision_response(0.4, 0.4, ELEPHANT_STRENGTH))

    # The threshold at which Elephant Mario can no longer go THRU blocks (between 0.4-0.5)

    test_response("Elephant Pound on Dirt", BREAK, get_collision_response(0.5, 0.5, ELEPHANT_STRENGTH))

    test_response("Elephant Pound on Grass Block", BREAK, get_collision_response(0.6, 0.6, ELEPHANT_STRENGTH))

    # The threshold at which Elephant Mario can no longer BREAK blocks



    print(
        f"FINAL RESULTS:\n\t{testsPassed}/{testsRun} passed ({round(testsPassed / testsRun * 100, 1)}%)\n\t{testsRun - testsPassed} failed")
