ELEPHANT_MARIO_COLLISION_STRENGTH = 5
SUPER_MARIO_COLLISION_STRENGTH = 4
SMALL_MARIO_COLLISION_STRENGTH = 3

def get_bap_result(hardness, blast_resistance, strength):
    if hardness < strength * 0.1:
        return THRU

    if hardness < strength * 0.125:
        return BREAK

    if hardness < strength * 0.375:
        return CRACK

    if strength <= 2:
        bump_threshold = 0
    elif strength == 3:
        bump_threshold = 3
    else:
        bump_threshold = 1 + strength * 0.5

    if hardness < bump_threshold:
        return BUMP

    return NO_SELL

NO_SELL, BUMP, CRACK, BREAK, THRU = range(5)
bapResultNames = ["NO-SELL", "BUMP", "CRACK", "BREAK", "GO-THRU"]

testsRun, testsPassed = 0, 0
def test_bap(test_name, expected_result, got_result):
    global testsRun, testsPassed
    testsRun += 1
    if type(expected_result) is tuple:
        acceptable_results = ""
        for acceptable_result in expected_result:
            if len(acceptable_results) != 0:
                acceptable_results += ", "
            acceptable_results += bapResultNames[acceptable_result]

        if got_result in expected_result:
            print(f"Test {test_name} PASSED (Got {bapResultNames[got_result]}, which is OK)\n")
            testsPassed += 1
        else:
            print(
                f"* Test {test_name} FAILED\n\tGot {bapResultNames[got_result]}, expected one of ({acceptable_results})\n")
    else:
        if expected_result == got_result:
            print(f"Test {test_name} PASSED (Got expected {bapResultNames[got_result]})\n")
            testsPassed += 1
        else:
            print(
                f"* Test {test_name} FAILED\n\tGot {bapResultNames[got_result]}, expected {bapResultNames[expected_result]}\n")


if __name__ == "__main__":
    print("Testing the bap calculator:")

    test_bap("Super Pound on Moss Block", THRU, get_bap_result(0.1, 0.1, SUPER_MARIO_COLLISION_STRENGTH))
    test_bap("Small Pound on Moss Block", (BREAK, THRU), get_bap_result(0.1, 0.1, 3))

    test_bap("Super Pound on Leaves", (BREAK, THRU), get_bap_result(0.2, 0.2, SUPER_MARIO_COLLISION_STRENGTH))  # Always passes
    test_bap("Small Pound on Leaves", BREAK, get_bap_result(0.2, 0.2, SMALL_MARIO_COLLISION_STRENGTH))

    test_bap("Elephant Pound on Netherrack", THRU, get_bap_result(0.4, 0.4, ELEPHANT_MARIO_COLLISION_STRENGTH))
    test_bap("Super Pound on Netherrack", BREAK, get_bap_result(0.4, 0.4, SUPER_MARIO_COLLISION_STRENGTH))
    test_bap("Small Pound on Netherrack", CRACK, get_bap_result(0.4, 0.4, SMALL_MARIO_COLLISION_STRENGTH))

    test_bap("Super Pound on Dirt", BREAK, get_bap_result(0.5, 0.5, SUPER_MARIO_COLLISION_STRENGTH))
    test_bap("Small Pound on Dirt", (CRACK, BUMP), get_bap_result(0.5, 0.5, SMALL_MARIO_COLLISION_STRENGTH))

    test_bap("Super Pound on Grass Block", CRACK, get_bap_result(0.6, 0.6, SUPER_MARIO_COLLISION_STRENGTH))
    test_bap("Small Pound on Grass Block", BUMP, get_bap_result(0.6, 0.6, SMALL_MARIO_COLLISION_STRENGTH))

    super_pound_stone = get_bap_result(1.5, 6, SUPER_MARIO_COLLISION_STRENGTH)
    test_bap("Super pound on Stone", (CRACK, BUMP), super_pound_stone)
    # Whatever Super Mario does to Stone, Small Mario should be 1 tier weaker
    test_bap("Small pound on Stone", max(0, super_pound_stone - 1), get_bap_result(1.5, 6, SMALL_MARIO_COLLISION_STRENGTH))

    test_bap("Super Pound on Wooden Planks", BUMP, get_bap_result(2, 3, SUPER_MARIO_COLLISION_STRENGTH))
    test_bap("Small Pound on Wooden Planks", BUMP, get_bap_result(2, 3, SMALL_MARIO_COLLISION_STRENGTH))

    test_bap("Super Pound on Iron Block", NO_SELL, get_bap_result(5, 6, SUPER_MARIO_COLLISION_STRENGTH))

    print(f"FINAL RESULTS:\n\t{testsPassed}/{testsRun} passed ({round(testsPassed / testsRun * 100, 1)}%)\n\t{testsRun - testsPassed} failed")
