import json
import os
import re

from Input import rename_sounds, content_subtitles, mod_subtitles

def handle_audio_files(include_voicelines, subtitle_source):
    # "../../mod/src/main/java/com/fqf/mario_qua_mario"
    voice_subtitles = []

    movement_sfx_subtitles = []
    power_up_sfx_subtitles = []
    stomp_sfx_subtitles = []
    action_sfx_subtitles = []

    last_added_character = ""

    for file_name in os.listdir("Sounds"):
        print(f"Checking '{file_name}'...")

        attempt_sfx(file_name, subtitle_source.sfx_movement, "movement", movement_sfx_subtitles)
        attempt_sfx(file_name, subtitle_source.sfx_power_up, "power_up", power_up_sfx_subtitles)
        attempt_sfx(file_name, subtitle_source.sfx_stomp, "stomp", stomp_sfx_subtitles)
        attempt_sfx(file_name, subtitle_source.sfx_action, "action", action_sfx_subtitles)

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

                new_subtitle = f'"subtitles.mario_qua_mario.voice.{character}.{voiceline}": "{character.title()} {content_subtitles.voicelines[voiceline]}"'
                if not new_subtitle in voice_subtitles and content_subtitles.voicelines[voiceline] != "SKIP":
                    voice_subtitles.append(new_subtitle)

    returnValue = []
    if(movement_sfx_subtitles): returnValue += movement_sfx_subtitles
    if(power_up_sfx_subtitles): returnValue += [""] + power_up_sfx_subtitles
    if(stomp_sfx_subtitles): returnValue += [""] + stomp_sfx_subtitles
    if(action_sfx_subtitles): returnValue += [""] + action_sfx_subtitles
    if(voice_subtitles): returnValue += voice_subtitles
    return returnValue

def attempt_sfx(file_name, subtitles_list, prefix, to_list):
    sfx_name = file_name[:-4]
    if sfx_name in rename_sounds.sfx: sfx_name = rename_sounds.sfx[sfx_name]

    if sfx_name in subtitles_list:
        to_list.append(f'"subtitles.mario_qua_mario.{prefix}.{sfx_name}": "{subtitles_list[sfx_name]}"')

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

# Example usage
if __name__ == "__main__":
    # Handle CONTENT sounds
    subtitles = handle_audio_files(True, content_subtitles)
    save_subtitles("Input/testInput.json", "Output/content/en_us.json", subtitles)

    # Handle MOD sounds
    subtitles2 = handle_audio_files(False, mod_subtitles)
    save_subtitles("Input/testInput.json", "Output/mod/en_us.json", subtitles2)
