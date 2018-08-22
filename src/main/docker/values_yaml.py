import sys
from ruamel.yaml import YAML
import argparse


def set_map_item(follow_list, delta_map, value):
    # len of key_list must >= 1
    def get_map(key_list, follow_map):
        if len(key_list) == 1:
            return follow_map
        if not key_list[0] in follow_map.keys():
            follow_map[key_list[0]] = {}
        if len(key_list) > 1:
            new_list = key_list[1:]
            return get_map(new_list, follow_map[key_list[0]])
        else:
            return follow_map[key_list[0]]
    inner_map = get_map(follow_list, delta_map)
    inner_map[follow_list[len(follow_list)-1]] = value



def traversal(version_value_map, deploy_value_map, follow_keys, delta_map, update_list, add_list):

    for key in deploy_value_map:
        follow_keys_copy = list(follow_keys)
        follow_keys_copy.append(key)
        # check version values if exit the same key
        if type(deploy_value_map[key]).__name__ == 'CommentedMap':
            if key in version_value_map.keys():
                if type(version_value_map[key]).__name__ == 'CommentedMap':
                    if len(version_value_map[key].keys()) == 0:
                        # version exist and is empty
                        version_value_map[key] = deploy_value_map[key]
                        add_list.append(follow_keys_copy)
                        set_map_item(follow_keys_copy, delta_map, dict(deploy_value_map[key]))

                    else:
                        traversal(version_value_map[key], deploy_value_map[key], follow_keys_copy, delta_map, update_list, add_list)
            else:
                # todo
                add_list.append(follow_keys_copy)
                version_value_map[key] = deploy_value_map[key]
                set_map_item(follow_keys_copy, delta_map, dict(deploy_value_map[key]))
        elif type(deploy_value_map[key]).__name__ == 'str' or type(deploy_value_map[key]).__name__ == 'int':
            # check if exist
            if key in version_value_map.keys():
                if (type(deploy_value_map[key]).__name__ == 'str' or type(deploy_value_map[key]).__name__ == 'int') and (version_value_map[key] != deploy_value_map[key]):
                    # not equal,replace
                    #
                    update_list.append(follow_keys_copy)
                    version_value_map[key] = deploy_value_map[key]
                    set_map_item(follow_keys_copy, delta_map, deploy_value_map[key])
            else:
                # add new str
                add_list.append(follow_keys_copy)
                version_value_map[key] = deploy_value_map[key]
                set_map_item(follow_keys_copy, delta_map, deploy_value_map[key])
        elif type(deploy_value_map[key]).__name__ == 'CommentedSeq':
            # check if exist
            if key in version_value_map.keys():
                if type(version_value_map[key]).__name__ == 'CommentedSeq':
                    # change list
                    add_list.append(follow_keys_copy)
                    version_value_map[key] = deploy_value_map[key]
                    set_map_item(follow_keys_copy, delta_map, deploy_value_map[key])
            else:
                # add list
                add_list.append(follow_keys_copy)
                version_value_map[key] = deploy_value_map[key]
                set_map_item(follow_keys_copy, delta_map, deploy_value_map[key])




def main():
    yaml = YAML()
    file_name = sys.argv[1]
    file_in = open(file_name).read()
    docs = yaml.load_all(file_in)
    i = 0
    for doc in docs:

        if i == 0:
            code_old = doc
        else:
            code_new = doc
        i = i + 1
    delta_map = dict()
    follow_keys = list()

    add = list()
    update = list()
    traversal(code_old, code_new, follow_keys, delta_map, update, add)
    yaml.dump(code_old, sys.stdout)

    print("------love----you------choerodon----")
    yaml.dump(delta_map, sys.stdout)

    print("------love----you------choerodon----")
    change_key_map = dict()

    change_key_map["add"] = add
    change_key_map["update"] = update
    yaml.dump(change_key_map, sys.stdout)


if __name__ == '__main__':
    main()
