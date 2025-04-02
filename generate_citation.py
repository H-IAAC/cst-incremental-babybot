import re
try:
    import cffconvert
except:
    import warnings
    warnings.warn("cffconvert not installed. Not updating README citation.")

    exit()

with open("CITATION.cff", "r") as file:
    cff_content = file.read()
    citation = cffconvert.Citation(cff_content)

citation_str = citation.as_bibtex(reference="Rossi-LL-RL-ICDL25")
citation_str = citation_str.replace("@misc", "@software")

citation_str = ("<!--CITATION START-->\n"+
                "```bibtext\n"+
                citation_str+
                "```\n"+
                "<!--CITATION END-->")

print(citation_str)
with open("README.md", "r") as file: 
    readme_text = file.read()

readme_text = re.sub(r"<!--CITATION START-->((.|\n)*)<!--CITATION END-->", citation_str, readme_text)

with open("README.md", "w") as file:
    file.write(readme_text)

print("Done")