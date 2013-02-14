from fabric.api import local
import json
import shutil
import os

def _get_json(path):
	json_data=open(path)
	data = json.load(json_data)
	return data

def _write_out(template_path, json_path, out_path):
	json_data = _get_json(json_path)
	f = open(template_path, "r")
	template = f.read()
	f.close()
	f = open(out_path, "w")
	f.write(template % json_data)
	f.close()
	
#env = {}
#buildconf = _get_json("buildconf.json")

#def ptm():
#	env["skin"] = "ptm"
#
#def greenprint():
#	env["skin"] = "greenprint"
#
#def treezilla():
#	env["skin"] = "treezilla"
#
#def ufm():
#	env["skin"] = "ufm"
#
#def default():
#	env["skin"] = "default"



#	<choices>
#		<choice key="action">
#			<option type="int" value="1">Watered</option>
#			<option type="int" value="2">Pruned</option>
#			<option type="int" value="3">Fruit or Nuts Harvested</option>
#			<option type="int" value="4">Removed</option>
#			<option type="int" value="5">Inspected</option>
#		</choice>	

def convert_choices_to_xml(choices_py):
	globs = {}
	execfile(choices_py, globs)
	choices = globs['CHOICES']

	#print choices

	hippie_xml = """
<choices>
  <choice key="bool_set">
    <option type="bool" value="1">Yes</option>
    <option type="bool" value="0">No</option>
  </choice>	
"""

	#print choices["canopy_conditions"]

	for (ch, vals) in choices.iteritems():
		#print vals
		hippie_xml += """  <choice key="%s">\n""" % ch
		for (value, txt) in vals:
			hippie_xml += """    <option type="int" value="%s">%s</option>\n""" % (value, txt)
		
		hippie_xml += "  </choice>\n"

        #for (value, txt) in vals:
        #	print value
        #		hippie_xml += "  </choice>\n"
	hippie_xml += "</choices>\n"

	print(hippie_xml)

