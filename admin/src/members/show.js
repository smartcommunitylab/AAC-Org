import * as React from "react";
import { Show, SimpleShowLayout } from 'react-admin';
import { TextField, FunctionField, ArrayField, SingleFieldList } from 'react-admin';
import { EditButton, ListButton, DeleteButton, TopToolbar } from 'react-admin';

import { TextInput, SimpleFormIterator, FormDataConsumer } from 'react-admin'

import List from '@material-ui/core/List';
import ListItem from '@material-ui/core/ListItem';
import ListItemIcon from '@material-ui/core/ListItemIcon';
import ListItemText from '@material-ui/core/ListItemText';

import InputLabel from '@material-ui/core/InputLabel';
import FormControl from '@material-ui/core/FormControl';

//members icon
import PeopleIcon from '@material-ui/icons/People';
//spaces icon
import DnsIcon from '@material-ui/icons/Dns';
//components icon
import AppsIcon from '@material-ui/icons/Apps';
//org icon
import AccountBalanceIcon from '@material-ui/icons/AccountBalance';

const extractIcon = (record = {}) => {
    switch (record.type) {
        case 'organization': return <AccountBalanceIcon />;
        case 'space': return <DnsIcon />;
        case 'component': return <AppsIcon />;
        default: return false;
    }
}

const RoleField = (props) => {

    console.log("in role field")
    console.dir(props)

    return (
        <span>
            <TextField label="type" source='type' {...props} />
            <TextField label="role" source='role' {...props} />
        </span>
    )
}

const RolesField = ({ record, resource, basePath, ...rest }) => {

    console.log("in role field")

    return (

        <List disablePadding={true}>
            {record.roles.map(role => {
                let label = role.role
                if (role.space) {
                    label = role.space + '/' + label
                }
                if (role.component) {
                    label = role.component + '/' + label
                }

                return (
                    <ListItem>
                        <ListItemIcon>
                            {extractIcon(role)}
                        </ListItemIcon>
                        <ListItemText primary={label} secondary={role.type} />
                    </ListItem>
                )
            }
            )}
        </List>


    )
}

RolesField.defaultProps = {
    addLabel: true,
    label: 'Roles',
};

const ShowActions = ({ basePath, data, resource }) => (
    <TopToolbar>
        <EditButton basePath={basePath} record={data} />
        <DeleteButton basePath={basePath}
            record={data}
            resource={resource}
            undoable={false}
        />
        <ListButton basePath={basePath} />

    </TopToolbar>
);


const MemberShow = ({ permissions, record, ...props }) => (
    <Show actions={<ShowActions />} {...props}>
        <SimpleShowLayout>
            <FunctionField addLabel={false} render={record => <h1> {record.fullName} </h1>} />
            <TextField source="organization" />
            <TextField source="username" />
            {/* <ArrayField source="roles">
                <SingleFieldList linkType={false}>
                    <RoleField />
                </SingleFieldList>
            </ArrayField> */}
            <RolesField />
        </SimpleShowLayout>
    </Show>
);

export default MemberShow;