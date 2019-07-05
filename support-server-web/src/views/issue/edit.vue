<template>
  <issue-detail :is-edit="true"/>
</template>

<script>
import IssueDetail from './components/IssueDetail'

export default {
  name: 'EditIssue',
  components: { IssueDetail }
}
</script>

