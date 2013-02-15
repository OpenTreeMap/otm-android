from fabric.api import local
from fabric.api import lcd
import json
import os
from getpass import getpass
import time

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
buildconf = _get_json("/var/jenkins/static/otm_android_buildconf.json")
env["otm_lib_path"] = buildconf["paths"]["src"]["otm_lib"]

def ptm():
	env["skin"] = "ptm"

def gp():
	env["skin"] = "gp"

def tz():
	env["skin"] = "tz"

def ufm():
	env["skin"] = "ufm"

def debug():
	env["release"] = False
	env["project_path"] = buildconf["paths"]["src"][env["skin"]]
	env["gmap_api_key"] = buildconf["gmap_api_keys"]["debug"][env["skin"]]
		
def release():
	env["release"] = True
	env["project_path"] = buildconf["paths"]["src"][env["skin"]]
	env["gmap_api_key"] = buildconf["gmap_api_keys"]["release"][env["skin"]]

def verify_paths():
	if os.path.isdir(env["project_path"]):
		print "Using path '%s' for skin '%s'." % (env["project_path"], env["skin"])
	else:
		raise "Project path '%s' for '%s' does not exist.  Check buildconf." % ( env["project_path"], env["skin"])
	if os.path.isdir(env["otm_lib_path"]):
		print "Using otm lib path '%s'" % (env["otm_lib_path"])
	else:
		raise "OTM lib path '%s' not found." % env["otm_lib_path"]

def convert_choices_to_xml(choices_py):
	globs = {}
	execfile(choices_py, globs)
	choices = globs['CHOICES']

	hippie_xml = """
<choices>
  <choice key="bool_set">
    <option type="bool" value="1">Yes</option>
    <option type="bool" value="0">No</option>
  </choice>	
"""
	for (ch, vals) in choices.iteritems():
		#print vals
		hippie_xml += """  <choice key="%s">\n""" % ch
		for (value, txt) in vals:
			hippie_xml += """    <option type="int" value="%s">%s</option>\n""" % (value, txt)
		
		hippie_xml += "  </choice>\n"

	hippie_xml += "</choices>\n"

	print(hippie_xml)


def install_gmap_api_key():
	if env["gmap_api_key"] == "":
		raise "missing api key"

	xml = """<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="gmap_api_key">%s</string>
</resources>""" % env["gmap_api_key"]
	path = env["project_path"] + "/res/values/gmap_api_key.xml"
	f = open(path, 'w')
	f.write(xml)
	f.flush()
	f.close()

def build_apk():
	sdk_path = buildconf["paths"]["android_sdks"]
	if os.path.isdir(sdk_path):
		print "Using '%s' for android sdk path." % sdk_path
	else:
		raise "It doesn't look like '%s' is the right path to the android sdks." % sdk_path

	with lcd(env["project_path"]):
		if env["release"]:
			cmd = "ant -Dsdk.dir=%s release" % sdk_path
			local(cmd)
		else:
			cmd = "ant -Dsdk.dir=%s debug" % sdk_path
			local(cmd)
		

#TODO
# release apk is being named TabLayout
def sign_apk():
	if not env["release"]:
		return

	try:
		cert_pw = buildconf["constants"][env["skin"]]
	except:
		pass
	if not cert_pw:
		cert_pw = getpass("Please enter the password for certificate: '%s'" % env["skin"])

	apk_path = buildconf["paths"]["apk"]["release"][env["skin"]]
	cert_path = buildconf["paths"]["release_certificates"][env["skin"]]
	keystore_alias = buildconf["keystore_alias"][env["skin"]]
	signed_apk_name = "%s/bin/%s-release-%s.apk" % (buildconf["paths"]["src"][env["skin"]], env["skin"], time.mktime(time.gmtime()))
	local("jarsigner -signedjar %s -keypass %s -storepass %s -verbose -sigalg MD5withRSA -digestalg SHA1 -keystore %s %s %s" % (signed_apk_name, cert_pw, cert_pw, cert_path, apk_path, keystore_alias))

def align_apk():
	#TODO
	pass

def deploy():
	if env["release"]:
		path = buildconf["paths"]["apk"]["release"][env["skin"]]
	else:
		path = buildconf["paths"]["apk"]["debug"][env["skin"]]
	
	if os.path.isfile(path):
		print "Attempting to install apk: %s" % path
		with lcd(env["project_path"]):
			local("adb install %s") % path
	else:
		raise "APK not found at: %s" % path


def build():
	verify_paths()
	install_gmap_api_key()
	build_apk()
	sign_apk()