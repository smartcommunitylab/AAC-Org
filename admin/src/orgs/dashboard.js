import * as React from "react";
import { cloneElement } from "react"
import { useShowController, SimpleShowLayout } from 'react-admin';
import { TextField, FunctionField, ReferenceField } from 'react-admin';

import { useQuery, Loading, Error } from 'react-admin';

import Card from '@material-ui/core/Card';
import CardContent from '@material-ui/core/CardContent';
import { Title } from 'react-admin';
import DeselectButton from "../fields/DeselectButton";
import { EditButton, ListButton, DeleteButton, TopToolbar, Toolbar } from 'react-admin';
import { MenuItemLink, Button } from "react-admin";


//members icon
import PeopleIcon from '@material-ui/icons/People';
//spaces icon
import DnsIcon from '@material-ui/icons/Dns';
//components icon
import AppsIcon from '@material-ui/icons/Apps';
//org icon
import AccountBalanceIcon from '@material-ui/icons/AccountBalance';

const OrganizationView = ({ record, resource = 'organizations', ...rest }) => {
    console.log("in view")
    console.dir(record)

    return (
        <React.Fragment>
            <SimpleShowLayout record={record} resource={resource} {...rest}>
                <FunctionField addLabel={true} label="organization" render={record => <h1> {record.name} </h1>} />
                <TextField source="id" />
                <TextField source="slug" />
            </SimpleShowLayout>
        </React.Fragment>
    )
};

const OrganizationDashboard = props => {
    const id = props.match.params.id
    const resource = "organizations"

    const { data, loading, error } = useQuery({
        type: 'getOne',
        resource: 'organizations',
        payload: { id: id }
    });

    if (loading) return <Loading />;
    if (error) return <Error />;
    if (!data) return null;

    return (
        <div>
            <Title title={data.name} />
            <TopToolbar>
                <DeselectButton record={data} resource={resource} />
            </TopToolbar>
            <Card>
                <CardContent>
                    <OrganizationView id={id} loaded={true} loading={false} record={data} resource={resource} />
                </CardContent>

                <Toolbar>
                    <MenuItemLink
                        key="spaces"
                        to="/spaces"
                        primaryText="Spaces"
                        leftIcon={<DnsIcon />}
                        //onClick={onMenuClick}
                        sidebarIsOpen={true}
                    />
                    <MenuItemLink
                        key="components"
                        to="/components"
                        primaryText="Components"
                        leftIcon={<AppsIcon />}
                        //onClick={onMenuClick}
                        sidebarIsOpen={true}
                    />
                    <MenuItemLink
                        key="members"
                        to="/members"
                        primaryText="Members"
                        leftIcon={<PeopleIcon />}
                        //onClick={onMenuClick}
                        sidebarIsOpen={true}
                    />
                </Toolbar>
            </Card>
        </div>
        // <OrganizationView {...props}>
        //     <SimpleShowLayout>
        //         <FunctionField addLabel={false} render={record => <h1> {record.name} </h1>} />
        //         <TextField source="id" />
        //         <TextField source="name" />
        //         <TextField source="slug" />
        //     </SimpleShowLayout>
        // </OrganizationView>
    )
}

export default OrganizationDashboard