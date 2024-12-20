import json
import os
import re
import shutil

from Input import rename_sounds, content_subtitles, mod_subtitles

def handle_audio_files(include_voicelines, copy_to, subtitle_source):
    # "../../mod/src/main/java/com/fqf/mario_qua_mario"
    voice_subtitles = []

    movement_sfx_subtitles = []
    power_up_sfx_subtitles = []
    stomp_sfx_subtitles = []
    action_sfx_subtitles = []

    last_added_character = ""

    for file_name in os.listdir("Sounds"):
        # print(f"Checking '{file_name}'...")

        attempt_sfx(file_name, subtitle_source.sfx_movement, "movement", movement_sfx_subtitles, copy_to)
        attempt_sfx(file_name, subtitle_source.sfx_power_up, "power_up", power_up_sfx_subtitles, copy_to)
        attempt_sfx(file_name, subtitle_source.sfx_stomp, "stomp", stomp_sfx_subtitles, copy_to)
        attempt_sfx(file_name, subtitle_source.sfx_action, "action", action_sfx_subtitles, copy_to)

        if(include_voicelines):
            match = re.match(r"(voc_)([a-z]+)(_)([a-z_]+)(\d*)(\.ogg)", file_name)
            if(match):
                character = match.group(2)
                voiceline = match.group(4)
                number = match.group(5)

                if voiceline in rename_sounds.voicelines:
                    voiceline = rename_sounds.voicelines[voiceline]

                if(character != last_added_character):
                    voice_subtitles.append("")
                    last_added_character = character

                if(content_subtitles.voicelines[voiceline] == "SKIP"):
                    continue

                new_file_home = f"{copy_to}/voices/{character}/{voiceline}"
                print(f"COPY VOICELINE TO: '{new_file_home}/{voiceline}{number}.ogg'")
                os.makedirs(new_file_home, exist_ok=True)
                shutil.copy(f"Sounds/{file_name}", f"{new_file_home}/{voiceline}{number}.ogg")

                new_subtitle = f'"subtitles.mario_qua_mario.voice.{character}.{voiceline}": "{character.title()} {content_subtitles.voicelines[voiceline]}"'
                if not new_subtitle in voice_subtitles:
                    voice_subtitles.append(new_subtitle)

    returnValue = []
    if(movement_sfx_subtitles): returnValue += movement_sfx_subtitles
    if(power_up_sfx_subtitles): returnValue += [""] + power_up_sfx_subtitles
    if(stomp_sfx_subtitles): returnValue += [""] + stomp_sfx_subtitles
    if(action_sfx_subtitles): returnValue += [""] + action_sfx_subtitles
    if(voice_subtitles): returnValue += voice_subtitles
    return returnValue

def attempt_sfx(file_name, subtitles_list, prefix, to_list, to_path):
    sfx_name = file_name[:-4]
    if sfx_name in rename_sounds.sfx: sfx_name = rename_sounds.sfx[sfx_name]

    if sfx_name in subtitles_list:
        to_list.append(f'"subtitles.mario_qua_mario.{prefix}.{sfx_name}": "{subtitles_list[sfx_name]}"')
        new_file_home = f"{to_path}/sfx/{prefix}"
        print(f"COPY SFX TO: '{new_file_home}/{sfx_name}.ogg'")
        os.makedirs(new_file_home, exist_ok=True)
        shutil.copy(f"Sounds/{file_name}", f"{new_file_home}/{sfx_name}.ogg")

def save_subtitles(input_file, output_file, new_subtitles):
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
    with open(output_file, 'w') as outfile:
        outfile.writelines(lines_to_keep)

        subtitle_count = len(new_subtitles)
        for incrementeroo in range(subtitle_count):
            new_subtitle = new_subtitles[incrementeroo]
            outfile.write("\n")
            if(new_subtitle == ""): continue
            outfile.write("  " + new_subtitle)
            if incrementeroo + 1 < subtitle_count: outfile.write(",")

        outfile.write("\n}")

    print(f"File saved successfully to: {output_file}")

def make_sounds_dot_json(location):
    print(f"Making sounds.json at {location}")

    sounds_dot_json = {

    }

    for sfx_category in os.listdir(f"{location}sounds/sfx"):
        for sfx in os.listdir(f"{location}sounds/sfx/{sfx_category}"):
            sfx_name = sfx[:-4]
            sounds_dot_json[f"sfx.{sfx_category}.{sfx_name}"] = {
                "subtitle": f"subtitles.mario_qua_mario.{sfx_category}.{sfx_name}",
                "sounds": [
                    f"mario_qua_mario:sfx/{sfx_category}/{sfx_name}"
                ]
            }

    print(f"Made sounds.json: {sounds_dot_json}")
    with open(f"{location}sounds.json", 'w', encoding='utf-8') as file:
        json.dump(sounds_dot_json, file, indent="\t")


if __name__ == "__main__":
    # Handle CONTENT sounds
    subtitles = handle_audio_files(True, "../../content/src/client/resources/assets/mario_qua_mario/sounds", content_subtitles)
    save_subtitles("../../content/src/client/resources/assets/mario_qua_mario/lang/en_us.json", "../../content/src/client/resources/assets/mario_qua_mario/lang/en_us.json", subtitles)
    make_sounds_dot_json("../../content/src/client/resources/assets/mario_qua_mario/")

    # Handle MOD sounds
    subtitles2 = handle_audio_files(False, "../../mod/src/client/resources/assets/mario_qua_mario/sounds", mod_subtitles)
    save_subtitles("../../content/src/client/resources/assets/mario_qua_mario/lang/en_us.json", "../../mod/src/client/resources/assets/mario_qua_mario/lang/en_us.json", subtitles2)
    make_sounds_dot_json("../../mod/src/client/resources/assets/mario_qua_mario/")
