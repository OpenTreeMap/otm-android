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

def default():
	env["skin"] = "default"

# operations
def colors():
	# REFACTOR Consider pulling from build/default/colors.json first, and overriding with skin values.
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

def drawables():
	# TARGET drawable directories
	hdpi_tgt   = buildconf["project"] + "/res/drawable-hdpi"
	ldpi_tgt   = buildconf["project"] + "/res/drawable-ldpi"
	mdpi_tgt   = buildconf["project"] + "/res/drawable-mdpi"	
	xhdpi_tgt  = buildconf["project"] + "/res/drawable-xhdpi"

	# start from a clean dir.  I decided this was preferable to having
	# stuff hanging around the eclipse project that is not going to be part
	# of a fresh build.
	try:
		shutil.rmtree(hdpi_tgt)
	except:
		pass
	try:
		shutil.rmtree(ldpi_tgt)
	except:
		pass
	try:
		shutil.rmtree(mdpi_tgt)
	except:
		pass
	try:
		shutil.rmtree(xhdpi_tgt)
	except:
		pass

	os.makedirs(hdpi_tgt)
	os.makedirs(ldpi_tgt)
	os.makedirs(mdpi_tgt)
	os.makedirs(xhdpi_tgt)

	# FIRST copy all files from default
	hdpi_src   = buildconf["default"] + "/drawable-hdpi/*"
	ldpi_src   = buildconf["default"] + "/drawable-ldpi/*"
	mdpi_src   = buildconf["default"] + "/drawable-mdpi/*"
	xhdpi_src  = buildconf["default"] + "/drawable-xhdpi/*"
	
	local("cp " + hdpi_src + " " + hdpi_tgt)
	local("cp " + ldpi_src + " " + ldpi_tgt)
	local("cp " + mdpi_src + " " + mdpi_tgt)
	local("cp  " + xhdpi_src + " " + xhdpi_tgt)

	# THEN overwrite with skin files
	skin = env["skin"]
	hdpi_src   = buildconf[skin] + "/drawable-hdpi/*"
	ldpi_src   = buildconf[skin] + "/drawable-ldpi/*"
	mdpi_src   = buildconf[skin] + "/drawable-mdpi/*"
	xhdpi_src  = buildconf[skin] + "/drawable-xhdpi/*"
	local("cp  " + hdpi_src + " " + hdpi_tgt)
	local("cp " + ldpi_src + " " + ldpi_tgt)
	local("cp  " + mdpi_src + " " + mdpi_tgt)
	local("cp  " + xhdpi_src + " " + xhdpi_tgt)

#composite operations
def build():
	colors()
	strings()


