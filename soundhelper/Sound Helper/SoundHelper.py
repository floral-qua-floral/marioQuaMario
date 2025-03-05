import json
import os
import re
import shutil

from Input import rename_sounds, content_subtitles, mod_subtitles, special_sound_events

def make_subtitles(include_voicelines, copy_to, subtitle_source):
    # Clean out the sounds that are currently lying around
    shutil.rmtree(copy_to)

    voicelines = []

    movement_sfx = []
    power_up_sfx = []
    stomp_sfx = []
    action_sfx = []

    empty_sound = ""

    last_added_character = ""

    for file_name in os.listdir("Sounds"):
        # print(f"Checking '{file_name}'...")

        attempt_sfx(file_name, subtitle_source.sfx_movement, "movement", movement_sfx, copy_to)
        attempt_sfx(file_name, subtitle_source.sfx_power_up, "power_up", power_up_sfx, copy_to)
        attempt_sfx(file_name, subtitle_source.sfx_stomp, "stomp", stomp_sfx, copy_to)
        attempt_sfx(file_name, subtitle_source.sfx_action, "action", action_sfx, copy_to)

        if(include_voicelines):
            match = re.match(r"(voc_)([a-z]+)(_)([a-z_]+)(\d*)(\.ogg)", file_name)
            if(match):
                character = match.group(2)
                voiceline = match.group(4)
                number = match.group(5)

                if voiceline in rename_sounds.voicelines:
                    voiceline = rename_sounds.voicelines[voiceline]

                if(character != last_added_character):
                    voicelines.append(empty_sound)
                    voicelines.append(f'"subtitles.mario_qua_mario.voice.{character}.backflip": "{character.title()} {content_subtitles.voicelines["backflip"]}"')
                    last_added_character = character

                if content_subtitles.voicelines[voiceline] == "SKIP":
                    continue

                new_file_home = f"{copy_to}/voices/{character}/{voiceline}"
                print(f"COPY VOICELINE TO: '{new_file_home}/{voiceline}{number}.ogg'")
                os.makedirs(new_file_home, exist_ok=True)
                shutil.copy(f"Sounds/{file_name}", f"{new_file_home}/{voiceline}{number}.ogg")

                new_subtitle = f'"subtitles.mario_qua_mario.voice.{character}.{voiceline}": "{character.title()} {content_subtitles.voicelines[voiceline]}"'
                if not new_subtitle in voicelines:
                    voicelines.append(new_subtitle)

    returnValue = []
    if movement_sfx: returnValue += movement_sfx
    if power_up_sfx: returnValue += [empty_sound] + power_up_sfx
    if stomp_sfx: returnValue += [empty_sound] + stomp_sfx
    if action_sfx: returnValue += [empty_sound] + action_sfx
    if voicelines: returnValue += voicelines
    return returnValue

def attempt_sfx(file_name, subtitles, prefix, to_list, to_path, allow_duping=False, source=""):
    sfx_name = file_name[:-4]
    if sfx_name in rename_sounds.sfx: sfx_name = rename_sounds.sfx[sfx_name]
    if source == "": source = sfx_name

    write_subtitle = True

    if sfx_name in subtitles:
        if allow_duping and sfx_name in special_sound_events.dupe_sfx:
            write_subtitle = False
            source = sfx_name
            for duped_name in special_sound_events.dupe_sfx[sfx_name]:
                to_list.append(f'"subtitles.mario_qua_mario.{prefix}.{duped_name}": "{subtitles[source]}"')

        if write_subtitle:
            to_list.append(f'"subtitles.mario_qua_mario.{prefix}.{sfx_name}": "{subtitles[sfx_name]}"')
        new_file_home = f"{to_path}/sfx/{prefix}"
        print(f"COPY SFX TO: '{new_file_home}/{sfx_name}.ogg'")
        os.makedirs(new_file_home, exist_ok=True)
        shutil.copy(f"Sounds/{file_name}", f"{new_file_home}/{sfx_name}.ogg")

def save_subtitles(input_file, output_directory, sounds):
    with open(input_file, 'r') as infile:
        lines = infile.readlines()

    lines_to_keep = []
    skip_next = False
    killed_prev_line = False

    # Iterate through lines with their index
    for incrementeroo in range(len(lines)):
        if skip_next or killed_prev_line:
            skip_next = False
            continue

        # Check if this is a subtitle
        if lines[incrementeroo].lstrip().startswith('"subtitles.'):
            if incrementeroo > 0:
                lines_to_keep.pop()
            skip_next = True
            killed_prev_line = True
        else:
            lines_to_keep.append(lines[incrementeroo])  # Keep the current line

    # Write the modified content to the output file
    os.makedirs(output_directory, exist_ok=True)
    with open(output_directory + "en_us.json", 'w') as outfile:
        outfile.writelines(lines_to_keep)

        subtitle_count = len(sounds)
        for incrementeroo in range(subtitle_count):
            new_subtitle = sounds[incrementeroo]
            outfile.write("\n")
            if new_subtitle == "": continue
            outfile.write("  " + new_subtitle)
            if incrementeroo + 1 < subtitle_count: outfile.write(",")

        outfile.write("\n}")

    print(f"File saved successfully to: {output_directory}")

def make_sounds_dot_json_and_java_file(sounds_dot_json_location, do_voices, input_java_file, java_file, sound_files_location):
    print(f"Making sounds.json at {sounds_dot_json_location}")

    sounds_dot_json = {}
    java_lines = []

    for sfx_category in os.listdir(f"{sound_files_location}sfx"):
        for sfx in os.listdir(f"{sound_files_location}sfx/{sfx_category}"):
            sfx_name = sfx[:-4]
            if sfx_name in special_sound_events.dupe_sfx:
                for dupe_name in special_sound_events.dupe_sfx[sfx_name]:
                    add_sound_to_json(sounds_dot_json, sfx_category, sfx_name, dupe_name, java_lines)
            else:
                add_sound_to_json(sounds_dot_json, sfx_category, sfx_name, sfx_name, java_lines)

        java_lines.append("\n")

    if do_voices:
        for character in os.listdir(f"{sound_files_location}voices"):
            for voiceline in os.listdir(f"{sound_files_location}voices/{character}"):
                voiceline_sound_files = []
                for sfx in os.listdir(f"{sound_files_location}voices/{character}/{voiceline}"):
                    sfx_name = sfx[:-4]
                    voiceline_sound_files.append(f"mario_qua_mario:voices/{character}/{voiceline}/{sfx_name}")

                sounds_dot_json[f"voice.{character}.{voiceline}"] = {
                    "subtitle": f"subtitles.mario_qua_mario.voice_{character}_{voiceline}",
                    "sounds": voiceline_sound_files
                }

                if voiceline == "sideflip": # Add backflip sound event; it just uses the Sideflip event
                    sounds_dot_json[f"voice.{character}.backflip"] = {
                        "subtitle": f"subtitles.mario_qua_mario.voice_{character}_backflip",
                        "sounds": [
                            {
                                "type": "event",
                                "name": f"mario_qua_mario:voice.{character}.sideflip"
                            }
                        ]
                    }

    print(f"Made sounds.json: {sounds_dot_json}")
    with open(f"{sounds_dot_json_location}sounds.json", 'w', encoding='utf-8') as file:
        # noinspection PyTypeChecker
        json.dump(sounds_dot_json, file, indent="\t")


    with open(input_java_file, 'r') as file:
        full_java_lines = file.readlines()

    modified_java_lines = full_java_lines[:9] + java_lines + full_java_lines[11:]

    with open(java_file, 'w', encoding='utf-8') as file:
        file.writelines(modified_java_lines)

def add_sound_to_json(sounds_dot_json, sfx_category, original_name, sfx_name, java_lines):
    sounds_dot_json[f"sfx.{sfx_category}.{sfx_name}"] = {
        "subtitle": f"subtitles.mario_qua_mario.{sfx_category}.{original_name}",
        "sounds": [
            f"mario_qua_mario:sfx/{sfx_category}/{original_name}"
        ]
    }
    java_category = sfx_category
    if java_category == "power_up": java_category = "PowerUp"
    else: java_category = java_category.capitalize()
    java_lines.append(f'\tpublic static final SoundEvent {sfx_name.upper()} = make{java_category}Sound("{sfx_name}");\n')

def handle_sound_set(
        include_voicelines,
        subtitle_script,
        sounds_dot_json_destination,
        input_java_file,

        output_java_file = "",
        audio_destination = "",
        old_lang_file = "",
        subtitle_destination = ""
):
    if output_java_file == "": output_java_file = input_java_file
    if audio_destination == "": audio_destination = sounds_dot_json_destination + "sounds"
    if old_lang_file == "": old_lang_file = sounds_dot_json_destination + "lang/en_us.json"
    if subtitle_destination == "": subtitle_destination = sounds_dot_json_destination + "lang/"

    subtitles = make_subtitles(include_voicelines, audio_destination, subtitle_script)
    save_subtitles(old_lang_file, subtitle_destination, subtitles)
    make_sounds_dot_json_and_java_file(sounds_dot_json_destination, include_voicelines, input_java_file, output_java_file, audio_destination + "/")

def get_sounds_dot_json_location(module):
    return f"../../{module}/src/client/resources/assets/mario_qua_mario/"
def get_java_file_location(module, addend):
    return f"../../{module}/src/main/java/com/fqf/mario_qua_mario/util/Mario{addend}SFX.java"

if __name__ == "__main__":
    handle_sound_set(True, content_subtitles, "Output/content/", "Input/MarioContentSfxClass.txt", "Output/content/MarioTestSFX.java.txt",
            old_lang_file = "Input/testInput.json")
    handle_sound_set(True, content_subtitles, get_sounds_dot_json_location("content"), "Input/MarioContentSfxClass.txt", get_java_file_location("content", "Content"))
    handle_sound_set(False, mod_subtitles, get_sounds_dot_json_location("mod"), "Input/MarioModSfxClass.txt", get_java_file_location("mod", ""))
