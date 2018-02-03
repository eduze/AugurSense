import json
import os
dataset_path = "/home/madhawa/Desktop/Augur/ImagesWithPositionsTestSet-PETS"
def loadMapping():
    categories = os.listdir(dataset_path)
    filtered_cats = []
    for cat in categories:
        if os.path.isdir(dataset_path + "/" + cat):
            filtered_cats.append(cat)

    mapping = {}
    for cat in filtered_cats:
        files = os.listdir(dataset_path + "/" + cat + "/")
        for file in files:
            if file not in mapping.keys():
                mapping[file] = []
            mapping[file].append(cat)
    return mapping

mapping = loadMapping()

def evaluateTP(tracked_person):
    labels = {}
    total = 0
    for p in tracked_person.tracking_history:
        if p.file_name in mapping:
            plabels = mapping[p.file_name]
            total += 1
            for label in plabels:
                if label in labels:
                    labels[label] += 1
                else:
                    labels[label] = 1
        else:
            print("Test set error")

    perc_labels = {}
    for label in labels.keys():
        perc_labels[label] = labels[label] / total

    return labels, perc_labels

def storeEvaluations(tracked_persons,time_frame):
    results_total = {}
    results_fraction = {}
    reduced_results_fractions = {}
    ind = 0
    for tp in tracked_persons:
        labels, perc_labels = evaluateTP(tp)
        results_total[ind] = labels
        results_fraction[ind] = perc_labels
        if len(perc_labels.values()) > 0:
            if max(list(perc_labels.values())) < 0.9:
                reduced_results_fractions[ind] = perc_labels
        ind += 1

    with open("re_id_saves/" + str(time_frame) + "/total.json","w") as f:
        f.write(json.dumps(results_total,indent=2))

    with open("re_id_saves/" + str(time_frame) + "/perc.json" ,"w") as f:
        f.write(json.dumps(results_fraction,indent=2))

    with open("re_id_saves/" + str(time_frame) + "/reduced_perc.json" ,"w") as f:
        f.write(json.dumps(reduced_results_fractions,indent=2))
