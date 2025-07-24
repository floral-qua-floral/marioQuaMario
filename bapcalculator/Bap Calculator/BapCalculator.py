def get_bap_result(hardness, force):
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
                f"* Test {test_name} FAILED ;-;\n\tGot {bapResultNames[got_result]}, expected one of ({acceptable_results})\n")
    else:
        if expected_result == got_result:
            print(f"Test {test_name} PASSED (Got expected {bapResultNames[got_result]})\n")
            testsPassed += 1
        else:
            print(
                f"* Test {test_name} FAILED ;-;\n\tGot {bapResultNames[got_result]}, expected {bapResultNames[expected_result]}\n")


if __name__ == "__main__":
    print("Testing the bap calculator:")

    test_bap("Super Pound on Moss Block", THRU, get_bap_result(0.1, 4))
    test_bap("Small Pound on Moss Block", (BREAK, THRU), get_bap_result(0.1, 3))

    test_bap("Super Pound on Leaves", (BREAK, THRU), get_bap_result(0.2, 4))  # Always passes
    test_bap("Small Pound on Leaves", BREAK, get_bap_result(0.2, 3))

    test_bap("Super Pound on Netherrack", BREAK, get_bap_result(0.4, 4))
    test_bap("Small Pound on Netherrack", (BREAK, CRACK), get_bap_result(0.4, 3))

    test_bap("Super Pound on Grass Block", CRACK, get_bap_result(0.6, 4))
    test_bap("Small Pound on Grass Block", BUMP, get_bap_result(0.6, 3))

    test_bap("Super pound on Stone", (CRACK, BUMP), get_bap_result(1.5, 4))
    test_bap("Small pound on Stone", (BUMP, NO_SELL), get_bap_result(1.5, 3))

    test_bap("Super Pound on Wooden Planks", BUMP, get_bap_result(2, 4))
    test_bap("Small Pound on Wooden Planks", BUMP, get_bap_result(2, 3))

    test_bap("Super Pound on Chest", BUMP, get_bap_result(2.5, 4))
    test_bap("Small Pound on Chest", (BUMP, NO_SELL), get_bap_result(2.5, 3))

    test_bap("Super Pound on End Stone, Copper, Doors, Gold", (BUMP, NO_SELL), get_bap_result(3, 4))
    test_bap("Small Pound on End Stone, Copper, Doors, Gold", (BUMP, NO_SELL), get_bap_result(3, 3))

    test_bap("Super Pound on Iron Block", NO_SELL, get_bap_result(5, 4))

    print(f"FINAL RESULTS:\n\t{testsPassed}/{testsRun} passed ({round(testsPassed / testsRun * 100, 1)}%)\n\t{testsRun - testsPassed} failed")
