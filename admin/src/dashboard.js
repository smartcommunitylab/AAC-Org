import * as React from "react";
import Card from '@material-ui/core/Card';
import CardContent from '@material-ui/core/CardContent';
import { Title } from 'react-admin';
import Divider from '@material-ui/core/Divider';
import Typography from '@material-ui/core/Typography';




export default ({ permissions }) => {

    if (permissions === undefined) {
        return <span>Loading..</span>
    }

    return (

        <Card>
            <Title title="OrgManager" />
            <CardContent>
                <Typography variant="h2" gutterBottom>
                    OrgManager
            </Typography>
                <Typography variant="body1" gutterBottom>
                    <p>Manage organizations within AAC: handle spaces, components and roles.</p>
                    <p>Create or select an organization from the list and then manage its entities.
                        When done, deselect the organization to get back to the list.</p>
                </Typography>
                {/* {permissions !== undefined ?
                    <Typography variant="body1" color="textSecondary" gutterTop gutterBottom>
                        You are logged in as {permissions.user['profile'].name}
                    </Typography> : null} */}

                <Typography variant="body1" color="textSecondary" gutterBottom>
                    You are logged in as {permissions.user['profile'].name}
                </Typography>

            </CardContent>
        </Card>
    )
};