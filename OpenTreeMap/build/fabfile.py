from fabric.api import local
import json

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
	
env = {}
buildconf = _get_json("buildconf.json")

# skins #
def ptm():
	env["skin"] = "ptm"

def greenprint():
	env["skin"] = "greenprint"

def treezilla():
	env["skin"] = "treezilla"

def ufm():
	env["skin"] = "ufm"

# operations
def colors():
	skin = env["skin"]
	json_path= buildconf[skin] + "/colors.json"
	template_path = buildconf["project"] + "/build/templates/colors.xml.template"
	out_path = buildconf["project"]+"/res/values/colors.xml"
	_write_out(template_path, json_path, out_path)


def strings():
	skin = env["skin"]
	json_path= buildconf[skin] + "/strings.json"
	template_path = buildconf["project"] + "/build/templates/strings.xml.template"
	out_path = buildconf["project"]+"/res/values/strings.xml"
	_write_out(template_path, json_path, out_path)

#bash all images with skin images
def drawables():
	pass

#composite operations
def build():
	colors()
	strings()


