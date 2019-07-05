<template>
  <issue-detail :is-edit="false"/>
</template>

<script>
import IssueDetail from './components/IssueDetail'

export default {
  name: 'CreateForm',
  components: { IssueDetail }
}
</script>

