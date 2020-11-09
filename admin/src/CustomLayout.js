import * as React from "react";
import { Layout, AppBar } from 'react-admin';

import { useSelector } from "react-redux";
import { useMediaQuery } from "@material-ui/core";
import { MenuItemLink, DashboardMenuItem, getResources, usePermissions } from "react-admin";
import DefaultIcon from "@material-ui/icons/ViewList";
import SettingsIcon from "@material-ui/icons/Settings";
import HomeIcon from "@material-ui/icons/Home";
import HelpIcon from "@material-ui/icons/Help";
import ExpandMore from '@material-ui/icons/ExpandMore';
import List from '@material-ui/core/List';
import Divider from '@material-ui/core/Divider';
import { connect } from 'react-redux';
import { Loading } from 'react-admin'

const CustomMenu = ({ onMenuClick, logout }) => {
    const isXSmall = useMediaQuery((theme) => theme.breakpoints.down("xs"));
    const open = useSelector((state) => state.admin.ui.sidebarOpen);
    const resources = useSelector(getResources);
    const { permissions } = usePermissions();

    const org = useSelector((state) => state.org);

    const subresources = ['spaces', 'components', 'members']


    if (!resources || !permissions) return <Loading />

    return (
        <div>
            {' '}

            <DashboardMenuItem
                onClick={onMenuClick}
                sidebarIsOpen={open}
            />

            {
                //generic resources
                permissions && 
                resources.map(
                    (resource) => {
                        if (!subresources.includes(resource.name) && resource.hasList) {
                            return (
                                <MenuItemLink
                                    key={resource.name}
                                    to={`/${resource.name}`}
                                    primaryText={
                                        (resource.options && resource.options.label) || resource.name
                                    }
                                    leftIcon={resource.icon ? <resource.icon /> : <DefaultIcon />}
                                    onClick={onMenuClick}
                                    sidebarIsOpen={open}
                                />
                            )
                        }
                    }
                )
            }


            {
                //subresources for selected org
                permissions && org &&
                <React.Fragment>
                    <Divider />
                    <MenuItemLink
                        key={"org-" + org.id}
                        to={`/dashboard/${org.id}`}
                        primaryText={org.name}
                        leftIcon={<ExpandMore />}
                        onClick={onMenuClick}
                        sidebarIsOpen={open}
                    />

                    <List component="div" disablePadding>
                        {
                            resources.map(
                                (resource) => {
                                    if (org && subresources.includes(resource.name) && resource.hasList) {

                                        return (
                                            <MenuItemLink
                                                key={resource.name}
                                                to={`/${resource.name}`}
                                                primaryText={
                                                    (resource.options && resource.options.label) || resource.name
                                                }
                                                leftIcon={resource.icon ? <resource.icon /> : <DefaultIcon />}
                                                onClick={onMenuClick}
                                                sidebarIsOpen={open}
                                            />
                                        )

                                    }
                                }
                            )
                        }
                    </List>
                </React.Fragment>
            }
            {isXSmall && logout}
        </div>
    );
};

const CustomAppBar = props => <AppBar {...props} color="primary" />;

// const CustomMenuOrg = props => <CustomMenu org={props.org} />

const CustomLayout = props => (
    < Layout
        {...props}
        appBar={CustomAppBar}
        menu={CustomMenu}
    />
)


// const mapStateToProps = state => {
//     console.log("map state for layout")
//     console.dir(state)
//     return {
//         org: state.org,
//     };
// };

export default CustomLayout;

// export default connect(mapStateToProps)(CustomLayout);